package ornamental.entities.abilities;

import arc.audio.*;
import arc.math.*;
import mindustry.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import ornamental.*;

// it's one hell of a workaround i know
public class EngineSoundAbility extends Ability {
    public static Sound loopSound = Sounds.minebeam;
    public static float
    loopSoundVolume = 0.2f, hitSizeVolumeScl = 0.01f, volumeAlpha = 0.2f,
    pitchAlpha = 0.2f, pitchScl = 1.5f;

    public float pitch;
    public float volume;

    public static void init() {
        Vars.content.units().each(u -> {
            u.abilities.add(new EngineSoundAbility());
        });
    }

    public EngineSoundAbility() {
        display = false;
    }

    @Override
    public void update(Unit unit) {
        if (!Vars.headless && (unit.elevation > 0f || !unit.type.useEngineElevation) && unit.type.loopSound == Sounds.none) {
            float newPitch = Mathf.lerp(pitch, unit.vel.len() * pitchScl, pitchAlpha);
            volume = Mathf.lerp(volume, unit.vel.isZero(0.001f) ? 0.5f : 1f, volumeAlpha);
            Ornamental.psound.loop(loopSound, unit, (loopSoundVolume + unit.hitSize * hitSizeVolumeScl) * volume, 0.5f + newPitch, 0.5f + pitch);
            pitch = newPitch;
        }
    }
}
