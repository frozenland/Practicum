#version 120

// Note: We multiply a vector with a matrix from the left side (M * v)!
// mProj * mView * mWorld * pos

// RenderCamera Input
uniform mat4 mViewProjection;

// RenderObject Input
uniform mat4 mWorld;
uniform mat3 mWorldIT;

// RenderMesh Input
attribute vec4 vPosition; // Sem (POSITION 0)
attribute vec3 vNormal; // Sem (NORMAL 0)
attribute vec2 vUV; // Sem (TEXCOORD 0)

varying vec4 worldPos; // vertex position in world-space coordinates

uniform float vTime;
uniform float vVelocity;

void main() {
    // Calculate Point In World Space
    vec4 newPos = vPosition - (vVelocity*vTime*.01);
    worldPos = mWorld * newPos;
    gl_Position = mViewProjection * worldPos;
}