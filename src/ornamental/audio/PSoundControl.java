package ornamental.audio;

import arc.*;
import arc.audio.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.pooling.*;
import arc.util.pooling.Pool.*;
import mindustry.*;

public class PSoundControl implements ApplicationListener {
    public float tolerance = 0.01f;

    public ObjectMap<Sound, Seq<PSoundData>> sounds = new ObjectMap<>();

    public void loop(Sound sound, Position pos, float volume, float pitch, float prevPitch) {
        if (Vars.headless) return;

        float baseVol = sound.calcFalloff(pos.getX(), pos.getY());
        float vol = baseVol * volume;

        Seq<PSoundData> seq = sounds.get(sound, Seq::new);
        PSoundData data = seq.find(d -> (Mathf.equal(prevPitch, d.pitch, Math.abs(prevPitch - pitch)) && d.updatePitch) || Mathf.equal(pitch, d.pitch, tolerance));

        if (data == null) {
            data = Pools.obtain(PSoundData.class, PSoundData::new);
            seq.add(data);
        }

        data.volume += vol;
        data.volume = Mathf.clamp(data.volume, 0f, 1f);
        data.total += baseVol;
        data.sum.add(pos.getX() * baseVol, pos.getY() * baseVol);

        if (data.updatePitch) {
            data.pitch = pitch;
            data.updatePitch = false;
        }
    }

    @Override
    public void update() {
        // lol
        updateLoops();
    }

    public void updateLoops() {
        if (!Vars.state.isGame()) {
            sounds.clear();
            return;
        }

        float avol = Core.settings.getInt("ambientvol", 100) / 100f;

        sounds.each((sound, seq) -> {
            seq.each(data -> {
                data.curVolume = Mathf.lerpDelta(data.curVolume, data.volume * avol, 0.11f);

                boolean play = data.curVolume > 0.01f;
                float pan = Mathf.zero(data.total, 0.0001f) ? 0f : sound.calcPan(data.sum.x / data.total, data.sum.y / data.total);
                if (data.soundID <= 0 || !Core.audio.isPlaying(data.soundID)) {
                    if (play) {
                        data.soundID = sound.loop(data.curVolume, data.pitch, pan);
                        Core.audio.protect(data.soundID, true);
                    }
                } else {
                    if (data.curVolume <= 0.001f) {
                        sound.stop();
                        seq.remove(data);
                        Pools.free(data);
                        return;
                    }

                    Core.audio.set(data.soundID, pan, data.curVolume);
                    Core.audio.setPitch(data.soundID, data.pitch);
                }

                data.volume = 0f;
                data.total = 0f;
                data.sum.setZero();
                data.updatePitch = true;
            });
        });
    }

    public static class PSoundData implements Poolable {
        float volume;
        float total;
        float pitch;
        boolean updatePitch = true;
        Vec2 sum = new Vec2();

        int soundID;
        float curVolume;

        @Override
        public void reset() {
            volume = total = pitch = 0f;
            updatePitch = true;
            sum.setZero();
            soundID = -1;
        }
    }
}
