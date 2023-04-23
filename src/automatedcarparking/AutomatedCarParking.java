/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking;

import automatedcarparking.misc.BasicContainer;
import automatedcarparking.misc.Container;
import automatedcarparking.misc.Game;

/**
 *
 * @author nikki
 */
public class AutomatedCarParking {
    
    private static FuzzifyKernel createKernel(String[] args) {
        if (args.length == 0) return new SudoFuzzify(2);
        if (args.length > 0) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(args[0]);
            } catch (ClassNotFoundException ex) {
                System.err.println("class `"+args[0]+"` not found");
                System.exit(-1);
            }
            Object o = null;
            try {
                o = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                System.err.println("Failed to instantiate class");
                System.exit(-1);
            }
            try {
                return (FuzzifyKernel)o;
            } catch (ClassCastException ex) {
                System.err.println("Class does not implement `FuzzifyKernel`");
                System.exit(-1);
            }
        }
        return null;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Create fuzzify kernel
        FuzzifyKernel kernel = createKernel(args);
        
        // Create game
        Game game = new Engine(kernel);
        
        // Create kernel
        Container container = new BasicContainer(game);
        
        // Run
        container.run(args);
    }
    
}
