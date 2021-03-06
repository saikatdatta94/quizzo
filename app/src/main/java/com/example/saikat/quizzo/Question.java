package com.example.saikat.quizzo;

public class Question {

    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String imgURL;
    private String isImg;
    private int priority;
    private int level;
    private int correct;

    public Question(){
//        Empty constructor needed
    }




    public Question(String question, String option1, String option2, String option3, String option4,
                    int priority, int level, int correct, String isImg, String imgURL) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.priority = priority;
        this.level = level;
        this.correct = correct;
        this.isImg = isImg;
        this.imgURL = imgURL;
    }

    public String getQuestion() {
        return question;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public int getPriority() {
        return priority;
    }

    public int getLevel() {
        return level;
    }
    public int getCorrect() {
        return correct;
    }

    public String getImgURL() {
        return imgURL;
    }

    public String getIsImg() {
        return isImg;
    }
}
