package com.example.taskrunner.service;


import org.springframework.stereotype.Component;
import java.util.List; import java.util.regex.Pattern;


@Component
public class CommandValidator {
private static final Pattern ALLOWED = Pattern.compile("^[a-zA-Z0-9\\s._/+:='\",!\\-]*$");
private static final List<String> BANNED = List.of(
"rm","sudo","chmod","chown","mkfs","dd","kill","pkill","reboot","shutdown","halt","init",
"telnet","nc","netcat","curl","wget","scp","ssh",">","<","|",";","&&","||","`","$((","&"
);
public void validateOrThrow(String command){
if (command==null || command.isBlank()) throw new IllegalArgumentException("Command must not be blank");
if (command.length()>256) throw new IllegalArgumentException("Command too long (max 256 chars)");
if (!ALLOWED.matcher(command).matches()) throw new IllegalArgumentException("Command contains disallowed characters");
String lc = command.toLowerCase();
for (String bad : BANNED) if (lc.contains(bad)) throw new IllegalArgumentException("Command contains banned token: "+bad);
}
}
