package net.rageevent.hypixel.game.instance;


import dev.datum.GameMap;
import dev.prism.api.entity.ClientEntity;
import dev.prism.api.world.Instance;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InstanceSetup {

    private final GameMap mapData;
    private final Instance gameInstance;

    public void setup(){


    }

    public void shutdown(){
        gameInstance.clientEntities().forEach(ClientEntity::remove);
    }

}
