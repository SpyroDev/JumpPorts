/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dwdg.jumpports.util;

/**
 *
 * @author Dan
 */
public class PortCommand {
    
    public enum Type {
        PLAYER, CONSOLE
    }
    
    private PortCommand.Type type = PortCommand.Type.PLAYER;
    private String command;
    
    public PortCommand(PortCommand.Type type, String command) {
        this.type = type;
        this.command = command;
    }
    
    
    public PortCommand.Type getCommandType() {
        return type;
    }
    
    public String getCommand() {
        return command;
    }
    
    public static PortCommand.Type getTypeFromString(String string) {
        if(string.toLowerCase().startsWith("c"))
            return PortCommand.Type.CONSOLE;
        return PortCommand.Type.PLAYER;
    }
    
    public void setType(PortCommand.Type type) {
        this.type = type;
    }
    
}
