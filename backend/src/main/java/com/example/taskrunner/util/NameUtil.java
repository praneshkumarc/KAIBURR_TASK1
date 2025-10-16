// util/NameUtil.java
package com.example.taskrunner.util;
public class NameUtil {
public static String toK8sName(String s){
if (s==null) return "x";
String cleaned = s.toLowerCase().replaceAll("[^a-z0-9-]","-").replaceAll("-+","-").replaceAll("(^-)|(-$)","");
return cleaned.isBlank()?"x":cleaned;
}
}