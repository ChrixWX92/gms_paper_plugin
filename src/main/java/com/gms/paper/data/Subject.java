package com.gms.paper.data;

import com.google.gson.Gson;

import java.util.HashMap;

public class Subject {
    public String _id;
    public String name;
    public Skill[] skills;

    private static Subject[] s_subjects;
    public static HashMap<String, Subject> s_subjectsLUT = new HashMap<>();

    public static Subject getSubject(String idOrName) {
        return s_subjectsLUT.get(idOrName);
    }

    public static void cacheSubjects() {
        if (s_subjects != null)
            return;

        String subjectJson = CmsApi.s_public.get("subjects", null);

        Gson gson = new Gson();
        s_subjects = gson.fromJson(subjectJson, Subject[].class);

        for (Subject subject : s_subjects) {
            s_subjectsLUT.put(subject._id, subject);
            s_subjectsLUT.put(subject.name, subject);

            for (Skill skill : subject.skills) {
                Skill.s_skillsLUT.put(skill._id, skill);
                Skill.s_skillsLUT.put(skill.name, skill);
            }
        }
    }
}
