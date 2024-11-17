---
id: register-animations
title: Реєстрація анімацій
sidebar_position: 6
---
# Реєстрація анімацій


Для створення класу animation ми будемо використовувати [JavaAnimation](https://repo.jodexindustries.xyz/javadoc/releases/com/jodexindustries/donatecase/spigot/2.2.6.7/raw/com/jodexindustries/donatecase/api/data/JavaAnimation.html) інтерфейс

##### Зверніть увагу на методи: [Case#animationPreEnd](https://repo.jodexindustries.xyz/javadoc/releases/com/jodexindustries/donatecase/spigot/2.2.6.7/raw/com/jodexindustries/donatecase/api/Case.html#animationPreEnd(com.jodexindustries.donatecase.api.data.casedata.CaseDataBukkit,org.bukkit.entity.Player,boolean,com.jodexindustries.donatecase.api.data.casedata.CaseDataBukkit.Item)) і [Case#animationEnd](https://repo.jodexindustries.xyz/javadoc/releases/com/jodexindustries/donatecase/spigot/2.2.6.7/raw/com/jodexindustries/donatecase/api/Case.html#animationEnd(com.jodexindustries.donatecase.api.data.casedata.CaseDataBukkit,org.bukkit.entity.Player,java.util.UUID,com.jodexindustries.donatecase.api.data.casedata.CaseDataBukkit.Item))
- [Case#animationPreEnd](https://repo.jodexindustries.xyz/javadoc/releases/com/jodexindustries/donatecase/spigot/2.2.6.7/raw/com/jodexindustries/donatecase/api/Case.html#animationPreEnd(com.jodexindustries.donatecase.api.data.casedata.CaseDataBukkit,org.bukkit.entity.Player,boolean,com.jodexindustries.donatecase.api.data.casedata.CaseDataBukkit.Item)) покликаний надати групу, відправити повідомлення і т.д.
- [Case#animationEnd](https://repo.jodexindustries.xyz/javadoc/releases/com/jodexindustries/donatecase/spigot/2.2.6.7/raw/com/jodexindustries/donatecase/api/Case.html#animationEnd(com.jodexindustries.donatecase.api.data.casedata.CaseDataBukkit,org.bukkit.entity.Player,java.util.UUID,com.jodexindustries.donatecase.api.data.casedata.CaseDataBukkit.Item)) викликається для завершення анімації.

> TestAnimation клас
```java
import com.jodexindustries.donatecase.api.Case;
import com.jodexindustries.donatecase.api.data.animation.JavaAnimation;
import org.bukkit.Bukkit;

public class TestAnimation extends JavaAnimation {
    @Override
    public void start() {
        Case.animationPreEnd(getCaseData(), getPlayer(), getUuid(), getWinItem());
        Bukkit.getScheduler().runTaskLater(Case.getInstance(), () -> Case.animationEnd(getCaseData(), getPlayer(), getUuid(), getWinItem()),20L);
    }
}

```

> Main клас
```java
    @Override
    public void onEnable() {
        // отримання CaseManager
        CaseManager api = new CaseManager(this);
        // реєстрація анімації
        AnimationManager animationManager = api.getAnimationManager();
        animationManager.registerAnimation("test", TestAnimation.class);
    }
```