package cn.redcdn.hvs.requesttreatment;

import cn.redcdn.datacenter.hpucenter.data.CurInfo;
import java.io.Serializable;

/**
 * Created by Administrator on 2017/11/23 0023.
 */

public class NewCurInfo extends CurInfo implements Serializable {

    public String name;

    public String cardType;

    public String card;

    public String mobile;

    public String age;

    public String gender;

    public String height;

    public String weight;

    public String introduction;

    public String inspection;

    public String check;

    public String toBeSolved;

    private Boolean isToUDT = false;

    private String time;


    public NewCurInfo() {
        this.name = "";
        this.cardType = "";
        this.card = "";
        this.mobile = "";
        this.age = "";
        this.gender = "";
        this.height = "";
        this.weight = "";
        this.introduction = "";
        this.inspection = "";
        this.check = "";
        this.toBeSolved = "";
        this.isToUDT = false;
        this.time = "";
    }

    public NewCurInfo(NewCurInfo info) {
        this.name = info.name;
        this.cardType = info.cardType;
        this.card = info.card;
        this.mobile = info.mobile;
        this.age = info.age;
        this.gender = info.gender;
        this.height = info.height;
        this.weight = info.weight;
        this.introduction = info.introduction;
        this.inspection = info.inspection;
        this.check = info.check;
        this.toBeSolved = info.toBeSolved;
        this.isToUDT = info.isToUDT;
        this.time = info.time;
    }

    public void setName(String Name){
        this.name = Name;
    }

    public String getName(){
        return this.name;
    }

    public void setCardType(String CardType){
        this.cardType = CardType;
    }

    public String getCardType(){
        return this.cardType;
    }

    public void setCard(String Card){
        this.card = Card;
    }

    public String getCard(){
        return this.card;
    }

    public void setMobile(String Mobile){
        this.mobile = Mobile;
    }

    public String getMobile(){
        return this.mobile;
    }

    public void setAge(String Age) {
        this.age = Age;
    }

    public String getAge() {
        return this.age;
    }

    public void setGender(String Gender) {
        this.gender = Gender;
    }

    public String getGender() {
        return this.gender;
    }

    public void setHeight(String Height) {
        this.height = Height;
    }

    public String getHeight() {
        return this.height;
    }

    public void setWeight(String Weight) {
        this.weight = Weight;
    }

    public String getWeight() {
        return this.weight;
    }

    public void setAbstract(String Abstract) {
        this.introduction = Abstract;
    }

    public String getAbstract() {
        return this.introduction;
    }

    public void setInspection(String Inspection) {
        this.inspection = Inspection;
    }

    public String getInspection() {
        return this.inspection;
    }

    public void setCheck(String Check) {
        this.check = Check;
    }

    public String getCheck() {
        return this.check;
    }

    public void setToBeSolved(String ToBeSolved) {
        this.toBeSolved = ToBeSolved;
    }

    public String getToBeSolved() {
        return this.toBeSolved;
    }

    public void setToUDT(Boolean toUDT) {
        isToUDT = toUDT;
    }

    public Boolean getToUDT() {
        return isToUDT;
    }

    public void setTime(String Time) {
        this.time = Time;
    }

    public String getTime() {
        return this.time;
    }

}
