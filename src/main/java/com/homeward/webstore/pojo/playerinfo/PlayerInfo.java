package com.homeward.webstore.pojo.playerinfo;

import com.homeward.webstore.pojo.BasePojo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PlayerInfo extends BasePojo {
    private String id;
    private String name;
    private List<Properties> properties;
    private Boolean legacy;
}