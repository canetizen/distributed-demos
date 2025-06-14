##
# Author: canetizen
# Created on Sat Jun 14 2025
# Description: CMake configuration
##

# Require CMake version 3.10 or higher
cmake_minimum_required(VERSION 3.10)

# Set the project name and declare use of C and C++
project(CycloneApp C CXX)

# Set C language standard to C11
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_C_EXTENSIONS OFF)

# Set C++ language standard to C++14
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Define the executable target and its source files
add_executable(app
    main.cpp                    # Main application logic
    generated/SensorData.c     # IDL-generated source
)

# Add include path for generated headers
target_include_directories(app PRIVATE generated)

# Link Cyclone DDS and pthread libraries
target_link_libraries(app
    ddsc       # Cyclone DDS C API
    pthread    # POSIX threads
)
