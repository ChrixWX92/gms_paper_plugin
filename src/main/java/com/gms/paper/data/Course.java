package com.gms.paper.data;

import com.gms.paper.util.Vector3D;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;

public class Course extends GenericContent {
    public static HashMap<String, Course> s_courses = new HashMap<>();

    public String title;
    public String description;
    public Date startDate;
    public Date endDate;
    public float minAge;
    public float maxAge;
    public boolean isActive;
    public String shortDescription;
    public Lesson[] lessons;
    public Platform platform;
    public String refId;

    @SerializedName("subject")
    public JsonObject _subjectId;
    private transient Subject _subject;
    private transient HashMap<String, Lesson> _lessons = new HashMap<>();

//    public Phrase phrases;
    public Vector3D spawnPos;

    public Subject getSubject() {
        if (_subject != null)
            return _subject;

        String subjectId;
        if (_subjectId.get("_id") != null)
            subjectId = _subjectId.get("_id").getAsString();
        else
            subjectId = _subjectId.getAsString();

        _subject = Subject.getSubject(subjectId);
        return _subject;
    }

    public String getLobbyWorldId() {
        return contentId + ".0";
    }

    public Lesson getLessonAt(int index) {
        cache();
        String lessonId = String.format("%s.%d", contentId, index);
        return _lessons.get(lessonId);
    }

    public Lesson getLesson(Player player, QuestionIdInfo idInfo) {
        cache();
        return _lessons.get(idInfo.lessonContentId);
    }

    public QuestionSet getQuestionSet(Player player, QuestionIdInfo idInfo) {
        Lesson lesson = getLesson(player, idInfo);
        if (lesson == null)
            return null;

        QuestionSet qset = lesson.getQuestionSet(player, idInfo);
        if (qset == null)
            return null;

        return qset;
    }

    public Question getQuestion(Player player, QuestionIdInfo idInfo, int progressLevel) {
        QuestionSet qset = getQuestionSet(player, idInfo);
        if (qset == null)
            return null;

        return qset.getQuestion(player, idInfo, progressLevel);
    }

    public Course cache() {
        /// If already cached then don't do a thing
        if (isCached())
            return this;

        /// Ok, first we need to get all the lessons
        for (Lesson lesson : lessons) {
//            lesson.cache();
            _lessons.put(lesson._id, lesson);
            _lessons.put(lesson.contentId, lesson);
        }

        return setCached();
    }

    public static Course cacheCourse(String contentId) {
        Course course = get(contentId);

        if (course == null)
            return null;

        return course.cache();
    }

    public static Course getId(String id) {
        return CmsApi.s_public.getContentFromId("courses", id, Course.class);
    }

    public static Course get(String contentId) {
        if (contentId.contains("-"))
            return getRefId(contentId);

        return CmsApi.s_public.getContent("courses", contentId, Course.class);
    }

    public static Course getRefId(String refId) {
        return CmsApi.s_public.getContentForKey("courses", "refId", refId, Course.class);
    }
}
