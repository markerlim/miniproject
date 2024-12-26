package com.project.mini_ssf.repo;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;


import jakarta.json.JsonObject;


@Repository
public class ListingRepo {

    @Qualifier("object")
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveToRedis(JsonObject jsonObject, String id) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put("listing",id,jsonObject.toString());
    }

    public Map<String,String> getAllListingFromRedis(){
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        return hashOps.entries("listing");
    }

    public String getOneListingFromRedis(String id){
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        return hashOps.get("listing", id);
    }
    public void saveUserListing(String userId,JsonObject list){
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put(userId, "listing", list.toString());
    }
    public String getuserPosting(String userId){
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        return hashOps.get(userId,"listing");
    }
}
