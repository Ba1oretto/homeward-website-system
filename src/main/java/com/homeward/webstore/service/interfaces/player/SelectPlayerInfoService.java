package com.homeward.webstore.service.interfaces.player;

import com.alibaba.fastjson.JSONObject;

public interface SelectPlayerInfoService {
    JSONObject getAPIMessage(String playerId);
    JSONObject getPlayerInfo(String playerUUID);
}