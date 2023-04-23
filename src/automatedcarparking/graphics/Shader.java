/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatedcarparking.graphics;

import static automatedcarparking.graphics.Util.resourceToByteBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

/**
 * Simple shader wrapper. Wraps a program with vertex and fragment shaders.
 * 
 * TODO:
 * interface, use 2.x and ARB if needed
 * @author nikki
 */
public class Shader {
            
    /**
     * Create a shader program from code.
     * 
     * @param vsCode char sequence containing the vertex shader code
     * @param fsCode char sequence containing the fragment shader code
     * @return a new shader object
     */
    public static Shader compile(CharSequence vsCode, CharSequence fsCode) {
        int vs = compile(vsCode, GL20.GL_VERTEX_SHADER);
        int fs = compile(fsCode, GL20.GL_FRAGMENT_SHADER);
        return create(vs, fs, true);
    }
    
    /**
     * Loads the shader program from a given resource. Convenience method for
     * loading shader from file.
     * 
     * @param vsResource resource location of the vertex shader
     * @param fsResource resource location of the vertex shader
     * @return a new shader object
     * @throws IOException 
     */
    public static Shader load(String vsResource, String fsResource) throws IOException {
        ByteBuffer vsSource = Util.resourceToByteBuffer(vsResource, 2048);
        ByteBuffer fsSource = Util.resourceToByteBuffer(fsResource, 2048);
        return create(vsSource, fsSource);
    }
    
    /**
     * Creates a shader program from (direct) buffers.
     * 
     * @param vsSource buffer containing the vertex shader source code
     * @param fsSource buffer containing the fragment shader source code
     * @return a new shader object
     */
    public static Shader create(ByteBuffer vsSource, ByteBuffer fsSource) {
        int vs = compile(vsSource, GL20.GL_VERTEX_SHADER);
        int fs = compile(fsSource, GL20.GL_FRAGMENT_SHADER);
        return create(vs, fs, true);
    }
    
    /**
     * Creates a shader program from shader objects. Used if you load your
     * own shader objects.
     * 
     * @param vertexShader vertex shader id
     * @param fragmentShader fragment shader id
     * @param deleteShaders whether to auto delete shader objects
     * @return a new shader objects
     */
    public static Shader create(int vertexShader, int fragmentShader, boolean deleteShaders) {
        int program = createProgramObject();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        link(program, vertexShader, fragmentShader, deleteShaders);
        return new Shader(program);
    }
    
    /**
     * Bind a shader.
     * 
     * @param shader shader to bind, can be null
     */
    public static void bind(Shader shader) {
        if (shader != null) {
            shader.bind();
        } else {
            GL20.glUseProgram(0);
        }
    }
    
    /**
     * Returns the GLSL version.
     * @return GLSL version
     */
    public static String version() {
        return GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION);
    }
    
    /** Creates a program object. */
    private static int createProgramObject() {
        int program = GL20.glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("Failed to create program");
        }
        return program;
    }
    
    /** Creates a shader object. */
    private static int createShaderObject(int type) {
        int shader = GL20.glCreateShader(type);
        if (shader == 0) {
            throw new RuntimeException("Failed create shader object");
        }
        return shader;
    }
    
    /** Compiles shader from char sequence. */
    private static int compile(CharSequence source, int type) {
        int shader = createShaderObject(type);
        GL20.glShaderSource(shader, source);
        return compile0(shader);
    }
    
    /** Compiles shader from buffer. */
    private static int compile(ByteBuffer source, int type) {
        int shader = createShaderObject(type);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer strings = stack.pointers(0L);
            strings.put(0, source);
            IntBuffer lengths = stack.ints(source.remaining());
            GL20.glShaderSource(shader, strings, lengths);
        }
        return compile0(shader);
    }
    
    /** Actually does the compilation, source must be uploaded. */
    private static int compile0(int shader) {
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            System.err.println(log);
            throw new RuntimeException("failed to compile shader");
        }
        return shader;
    }
    
    /** Links the program. */
    private static void link(int program, int vs, int fs, boolean deleteShaders) {
        GL20.glLinkProgram(program);
        if (deleteShaders) {
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
        }
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0) {
            String log = GL20.glGetProgramInfoLog(program);
            GL20.glDeleteProgram(program);
            System.err.println(log);
            throw new RuntimeException("Failed to link program.");
        }
    }
    
    private final int program;
    
    private Shader(int program) {
        this.program = program;
    }
  
    /** Binds this shader. */
    public void bind() {
        GL20.glUseProgram(program);
    }
    
    /** Unbinds this shader. */
    public void unbind() {
        GL20.glUseProgram(0);
    }
    
    /**
     * Returns the id.
     * @return the id
     */
    public final int id() {
        return program;
    }
    
    /** Perform cleanup operation and invalidate this shader. */
    public void dispose() {
        if (program != 0) {
            unbind();
            GL20.glDeleteProgram(program);
        }
    }
    
}
