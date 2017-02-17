cmake_minimum_required(VERSION 3.4.1)

add_library(bzlib STATIC
                blocksort.c
                huffman.c
                crctable.c
                randtable.c
                compress.c
                decompress.c
                bzlib.c)

add_library(bspatch STATIC
                bspatch.c
                bzlib.h)

add_library(Patcher SHARED
              patcher.c
              bspatch.h)

target_link_libraries(Patcher
                        bspatch
                        bzlib
                        android
                        log)
