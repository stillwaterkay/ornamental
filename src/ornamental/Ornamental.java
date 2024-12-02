package ornamental;

import arc.*;
import mindustry.mod.*;
import ornamental.audio.*;
import ornamental.entities.abilities.*;

public class Ornamental extends Mod {
    public static PSoundControl psound;

    @Override
    public void init() {
        Core.app.addListener(psound = new PSoundControl());

        EngineSoundAbility.init();
    }
}