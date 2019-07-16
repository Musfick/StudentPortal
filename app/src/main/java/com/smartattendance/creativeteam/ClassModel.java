package com.smartattendance.creativeteam;

public class ClassModel {

    public String code;
    public String date;
    public String section;
    public String semester;
    public String title;

    public ClassModel()
    {}

    public ClassModel(String code, String date, String section, String semester, String title) {
        this.code = code;
        this.date = date;
        this.section = section;
        this.semester = semester;
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
