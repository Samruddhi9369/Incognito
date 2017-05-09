package me.scryptminers.android.incognito.Util;

import java.util.regex.Pattern;

public class Validations {
    // Check if the entered email is valid or not
    public boolean isEmailValid(String email) {
        boolean errorFlag = true;
        Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        if(!emailPattern.matcher(email).find()){
            errorFlag = false;
        }
        return errorFlag;
    }

    // Check if the entered password is valid or not
    public boolean isPasswordValid(String password) {
        boolean errorFlag = true;
        Pattern specailCharPattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern upperCasePattern = Pattern.compile("[A-Z ]");
        Pattern lowerCasePattern = Pattern.compile("[a-z ]");
        Pattern digitCasePattern = Pattern.compile("[0-9 ]");
        if(password.length() < 8){
            errorFlag =false;
        } else if(!specailCharPattern.matcher(password).find()){
            errorFlag = false;
        } else if(!upperCasePattern.matcher(password).find()){
            errorFlag = false;
        } else if(!lowerCasePattern.matcher(password).find()){
            errorFlag = false;
        } else if(!digitCasePattern.matcher(password).find()){
            errorFlag = false;
        }
        return errorFlag;
    }

    // Check if the entered phone is valid or not
    public boolean isPhoneValid(String phone) {
        return phone.length() < 11;
    }

}
