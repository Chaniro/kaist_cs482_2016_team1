package com.kaist.icg.pacman.graphic;

import android.graphics.Bitmap;

import com.kaist.icg.pacman.tool.Material;

import java.nio.FloatBuffer;

/**
 * OBJ file parser
 */
public class Object3D extends Drawable {
    protected float[] color;
    protected boolean hasTexture;
    private int childIndexCounter;

    public Object3D(int vertexBufferSize, FloatBuffer vertexBuffer,
                    FloatBuffer normalBuffer) {
        this.vertexBufferSize = vertexBufferSize;
        this.vertexBuffer = vertexBuffer;
        this.normalBuffer = normalBuffer;
        this.hasTexture = false;
        this.color = new float[] {(float) Math.random(), (float) Math.random(), (float) Math.random()};

        material = new Material(color);
    }

    public Object3D(int vertexBufferSize, FloatBuffer vertexBuffer,
                    FloatBuffer normalBuffer, FloatBuffer textureCoordinatesBuffer) {
        this.vertexBufferSize = vertexBufferSize;
        this.vertexBuffer = vertexBuffer;
        this.normalBuffer = normalBuffer;
        this.textureCoordinatesBuffer = textureCoordinatesBuffer;
        this.hasTexture = true;
        this.color = new float[] {(float) Math.random(), (float) Math.random(), (float) Math.random()};

        material = new Material(color);
    }

    public void setTextureFile(String fileName) {
        if(this.textureCoordinatesBuffer == null)
            throw new RuntimeException("Assigned texture file to a mesh without UV mapping information");

        material = new Material(fileName);
    }

    public void setTexture(Bitmap bitmap) {
        if(this.textureCoordinatesBuffer == null)
            throw new RuntimeException("Assigned texture file to a mesh without UV mapping information");

        material = new Material(bitmap);
    }

    public Object3D(int vertexBufferSize, FloatBuffer vertexBuffer,
                    FloatBuffer normalBuffer, FloatBuffer textureCoordinatesBuffer, Bitmap texture) {
        this.vertexBufferSize = vertexBufferSize;
        this.vertexBuffer = vertexBuffer;
        this.normalBuffer = normalBuffer;
        this.textureCoordinatesBuffer = textureCoordinatesBuffer;
        this.hasTexture = true;

        material = new Material(texture);
    }

    /**
     * Draw the mesh on the current OpenGL context
     */
    @Override
    public void draw() {
        computeModelMatrix();

        shaderManager.draw(modelMatrix, vertexBuffer,
                normalBuffer, textureCoordinatesBuffer, vertexBufferSize,
                material, shader);

        for(childIndexCounter = 0; childIndexCounter <children.size(); childIndexCounter++)
            children.get(childIndexCounter).draw();
    }

    public void setColor(float[] color) {
        this.color = color;
    }
}