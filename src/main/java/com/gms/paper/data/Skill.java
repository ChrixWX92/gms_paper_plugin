package com.gms.paper.data;

import com.gms.paper.util.Helper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class Skill {
    public String _id;
    public String name;

    public static HashMap<String, Skill> s_skillsLUT = new HashMap<>();

    public static Skill find(String id) {
        return s_skillsLUT.get(id);
    }

    public static Skill resolveSkill(JsonObject skillObj) {
        String skillId = skillObj.getAsString();

        if (!Helper.isMongoId(skillId))
            return (new Gson()).fromJson(skillObj.toString(), Skill.class);

        return s_skillsLUT.get(skillId);
    }
}
