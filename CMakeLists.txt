PROJECT (jummp Java)
cmake_minimum_required(VERSION 2.8)
find_package(Java)
set(CMAKE_VERBOSE_MAKEFILE 1)
add_subdirectory(jummp-plugins)
set(JUMMP_PLUGIN_LIB_SRC_DIR ${CMAKE_CURRENT_SOURCE_DIR}/src/java/net/biomodels/jummp/core)

set(JUMMP_PLUGIN_LIB_SRC
${JUMMP_PLUGIN_LIB_SRC_DIR}/JummpException.java
${JUMMP_PLUGIN_LIB_SRC_DIR}/vcs/VcsException.java
${JUMMP_PLUGIN_LIB_SRC_DIR}/vcs/FileAlreadyVersionedException.java
${JUMMP_PLUGIN_LIB_SRC_DIR}/vcs/FileNotVersionedException.java
${JUMMP_PLUGIN_LIB_SRC_DIR}/vcs/VcsAlreadyInitedException.java
${JUMMP_PLUGIN_LIB_SRC_DIR}/vcs/VcsNotInitedException.java
${JUMMP_PLUGIN_LIB_SRC_DIR}/vcs/VcsManager.java
)

# cmake builds class files with wrong package path - adjust
set(JUMMP_PLUGIN_LIB_BUILD_DIR ${CMAKE_CURRENT_BINARY_DIR}/CMakeFiles/jummp-plugin-lib.dir/)
set(CMAKE_Java_COMPILE_OBJECT
  "<CMAKE_Java_COMPILER> <FLAGS> <SOURCE> -d ${JUMMP_PLUGIN_LIB_BUILD_DIR}"
)
# cmake would include also all cmake files, ensure that only package structure starting with net is included in jar
set(CMAKE_Java_CREATE_STATIC_LIBRARY
  "<CMAKE_Java_ARCHIVE> -cf <TARGET> -C <OBJECT_DIR> net")
# we need our own files during build
include_directories(
${JUMMP_PLUGIN_LIB_BUILD_DIR}
)
add_library(jummp-plugin-lib ${JUMMP_PLUGIN_LIB_SRC})

# install the generated plugins
# TODO: has to become a macro
# name of generated zip file is stored in a global property - pass it to a variable
get_property(JUMMP_SUBVERSION_PLUGIN GLOBAL PROPERTY JUMMP_SUBVERSION_PLUGIN)
# grails install-plugin
add_custom_command(OUTPUT grails-plugins
COMMAND grails install-plugin ${JUMMP_SUBVERSION_PLUGIN}
WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
)

# add target so that install plugin is executed
add_custom_target(
grails-plugins
ALL DEPENDS ${CMAKE_CURRENT_BINARY_DIR}/grails-plugins)

# Make sure the source is generated before the executable builds.
ADD_DEPENDENCIES(grails-plugins jummp-subversion-plugin)

# build war
# TODO: out of source war command to not run into the issue of compile errors in jummp-plugins
add_custom_command(OUTPUT ${CMAKE_CURRENT_BINARY_DIR}/jummp.war
COMMAND CLASSPATH=${JUMMP_PLUGIN_LIB_BUILD_DIR}:$ENV{CLASSPATH} grails war ${CMAKE_CURRENT_BINARY_DIR}/jummp.war
WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
)
add_custom_target(
grails-war
ALL DEPENDS ${CMAKE_CURRENT_BINARY_DIR}/jummp.war)
ADD_DEPENDENCIES(grails-war grails-plugins)
