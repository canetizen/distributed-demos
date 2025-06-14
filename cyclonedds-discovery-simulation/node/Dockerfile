##
 # Author: canetizen
 # Created on Sat Jun 14 2025
 # Description: Dockerfile for simulation
##

# === STAGE 1: Generate C source from IDL ===
FROM ubuntu:22.04 AS idlgen

# Install development tools and Cyclone DDS IDL compiler
RUN apt-get update && apt-get install -y \
    build-essential cmake git unzip curl \
    cyclonedds-tools

# Working directory for IDL processing
WORKDIR /gen

# Copy the IDL file (interface definition for SensorData)
COPY shared/SensorData.idl .

# Generate C source and header files from the IDL definition
RUN mkdir -p generated && \
    idlc -l c SensorData.idl && \
    mv SensorData.c SensorData.h generated/

# === STAGE 2: Build the application ===
FROM ubuntu:22.04

# Install development packages and Cyclone DDS runtime
RUN apt-get update && apt-get install -y \
    build-essential cmake git unzip curl \
    cyclonedds-dev

# Copy DDS configuration file
COPY shared/cyclonedds.xml /shared/cyclonedds.xml

# Set working directory for the application build
WORKDIR /app

# Copy application source files (e.g. main.cpp, CMakeLists.txt)
COPY node/ ./

# Copy the IDL-generated C files from stage 1
COPY --from=idlgen /gen/generated ./generated

# Configure and build the application using CMake
RUN cmake . && make
