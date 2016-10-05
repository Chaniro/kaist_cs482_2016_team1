package com.kaist.icg.pacman.graphic;

import android.opengl.GLES20;
import android.util.Log;

import com.kaist.icg.pacman.graphic.android.PacManActivity;
import com.kaist.icg.pacman.graphic.android.PacManGLRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * OBJ file parser
 */
public class Object3D extends Drawable {
    private int nbVertices;
    protected float[] color = {(float) Math.random(), (float) Math.random(), (float) Math.random()};

    private ArrayList<float[]> verticesDictionary;
    private ArrayList<float[]> normalsDictionary;
    private ArrayList<Face> facesDictionary;

    private int lightHandle;
    private int light2Handle;
    private int colorHandle;

    private float[] light = new float[3];
    private float[] light2 = new float[3];

    private int i;

    public Object3D(String file) {
        verticesDictionary = new ArrayList<>();
        normalsDictionary = new ArrayList<>();
        facesDictionary = new ArrayList<>();

        loadFile(file);
        buildBuffers();
        Log.d("Object3D", "[" + file + "] " + nbVertices + " vertices");

        // prepare shaders and OpenGL program
        int vertexShader = PacManGLRenderer.loadShaderFromFile(
                GLES20.GL_VERTEX_SHADER, "basic-gl2.vshader");
        int fragmentShader = PacManGLRenderer.loadShaderFromFile(
                GLES20.GL_FRAGMENT_SHADER, "diffuse-gl2.fshader");

        program = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(program, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(program, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(program);                  // create OpenGL program executables

        //Set light position
        light = new float[] {0.0f, 0.0f, 0.0f};
        light2 = new float[] {0.0f, 0.0f, 0.0f};
    }

    /**
     * Load OBJ file and build the model
     * @param file File path
     */
    private void loadFile(String file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    PacManActivity.context.getAssets().open(file)));

            String line;
            do {
                line = reader.readLine();
                if(line != null) {
                    if (line.startsWith("v ")) { //vertex
                        String[] split = line.split(" ");
                        float[] vertex = new float[3];
                        vertex[0] = Float.parseFloat(split[1]);
                        vertex[1] = Float.parseFloat(split[2]);
                        vertex[2] = Float.parseFloat(split[3]);

                        verticesDictionary.add(vertex);
                    } else if (line.startsWith("vn ")) { //normal
                        String[] split = line.split(" ");
                        float[] normal = new float[3];
                        normal[0] = Float.parseFloat(split[1]);
                        normal[1] = Float.parseFloat(split[2]);
                        normal[2] = Float.parseFloat(split[3]);

                        normalsDictionary.add(normal);
                    } else if (line.charAt(0) == 'f') { //face
                        facesDictionary.add(new Face(line));
                    }
                }
            } while (line != null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build vertices and normals buffer (FloatBuffer) from the model
     */
    private void buildBuffers() {
        nbVertices = 0;

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(facesDictionary.size() * 3 * 3 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuf.asFloatBuffer();

        for(Face face : facesDictionary) {
            for(int i = 0; i<face.getVerticesIndex().length; i++) {
                vertexBuffer.put(verticesDictionary.get(face.getVerticesIndex()[i]));
                nbVertices++;
            }
        }
        vertexBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(facesDictionary.size() * 3 * 3 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        normalBuffer = byteBuf.asFloatBuffer();

        for(Face face : facesDictionary) {
            for(int i = 0; i<face.getNormalsIndex().length; i++) {
                normalBuffer.put(normalsDictionary.get(face.getNormalsIndex()[i]));
            }
        }
        normalBuffer.position(0);
    }

    /**
     * Draw the mesh on the current OpenGL context
     * @param projectionMatrix
     * @param viewMatrix
     */
    @Override
    public void draw(float[] projectionMatrix, float[] viewMatrix) {
        GLES20.glUseProgram(program);
        prepareDraw(projectionMatrix, viewMatrix);

        // uniforms
        colorHandle = GLES20.glGetUniformLocation(program, "uColor");
        lightHandle = GLES20.glGetUniformLocation(program, "uLight");
        light2Handle = GLES20.glGetUniformLocation(program, "uLight2");

        GLES20.glUniform3fv(colorHandle, 1, color, 0);
        GLES20.glUniform3fv(lightHandle, 1, light, 0);
        GLES20.glUniform3fv(light2Handle, 1, light2, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, facesDictionary.size() * 3);

        endDraw();

        for(i = 0; i<children.size(); i++)
            children.get(i).draw(projectionMatrix, viewMatrix);
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    /**
     * Inner class which handle face information (3 vertices)
     */
    public class Face {
        private int[] verticesIndex;
        private int[] normalsIndex;

        public Face(String objLine)
        {
            verticesIndex = new int[3];
            normalsIndex = new int[3];

            String[] vertices = objLine.split(" ");
            String[] vertexInfo;

            for(int i = 1; i<4; i++) {
                vertexInfo = vertices[i].split("/");
                verticesIndex[i - 1] = Integer.parseInt(vertexInfo[0]) - 1;
                normalsIndex[i - 1] = Integer.parseInt(vertexInfo[2]) - 1;
            }
        }

        public int[] getVerticesIndex() {
            return verticesIndex;
        }

        public int[] getNormalsIndex() {
            return normalsIndex;
        }
    }
}