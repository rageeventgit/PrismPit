package net.rageevent.hypixel.player.enums;

import dev.prism.api.text.Text;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
@Getter
public enum AdminSetting {

    BUILD_MODE(Text.of("Build Mode"), Text.of("Break & place anything you want!"), true),
    RAINBOW_CHAT(Text.of("Rainbow Chat"), Text.of("Make your text colorful!"), false);

    private final Text title;
    private final Text description;
    private final boolean autoToggle;
}
