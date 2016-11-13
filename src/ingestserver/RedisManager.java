/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ingestserver;

import com.google.gson.Gson;
import java.util.Map;
import redis.clients.jedis.Jedis;

/* Redis Keys:
 * clip:ids - A single number used to generate a new id using INCR
 * clip:all - A list that stores all ids
 * clip:<id>:data -There will be one key with this pattern for each user in the system. Those keys will be hashes
 */
public class RedisManager {

    private Jedis redis;

    public RedisManager() {
        this.redis = new Jedis();
    }

    public Clip addClip(Clip clip) {

        long clipId = redis.incr("clip:ids");
        clip.setId(clipId);

        //maps object to map using JSON
        Gson gson = new Gson();
        String jsonClip = gson.toJson(clip);
        Map<String, String> clipMap = gson.fromJson(jsonClip, Map.class);
        System.out.println("INCR clip:ids = " + String.valueOf(clipId));

        //Getting the Pipeline
        //Pipeline pipeline = redis.pipelined();
        //add to clip list
        //pipeline.lpush("clip:all", String.valueOf(clipId));
        Long lpushresponse = redis.lpush("clip:all", String.valueOf(clipId));
        System.out.println("LPUSH clip:all = " + lpushresponse.toString());
        //add to the hash
        //pipeline.hmset("clip:" + String.valueOf(clipId) + ":data", clipMap);
        String hmstetresponse = redis.hmset("clip:" + String.valueOf(clipId), clipMap);
        System.out.println("HMSET clip:" + String.valueOf(clipId) + " = " + hmstetresponse);
        //pipeline.sync();
        return clip;
    }

    public void removeClip(String clipId) {
        String userInfoKey = redis.get("clip:" + clipId);
        if (userInfoKey != null) {
            //Pipeline pipeline = redis.pipelined();
            //Response<Long> responseDel = pipeline.del(userInfoKey);
            redis.del(userInfoKey);
            //Response<Long> responseLrem = pipeline.lrem("clip:all", 0, String.valueOf(clipId));
            redis.lrem("clip:all", 0, clipId);
            //pipeline.sync();
        }
    }

    public void resetRedisKeys() {
        if (redis.exists("clip:ids") && redis.exists("clip:all")) {
            int count = Integer.valueOf(redis.get("clip:ids"));
            do {
                redis.del("clip:" + count);
                count--;
            } while (count > 0);
            redis.del("clip:all");
            redis.del("clip:ids");
        }
    }

}
