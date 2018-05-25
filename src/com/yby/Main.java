package com.yby;

import java.io.File;

public class Main {

    public static void main(String[] args) {
	// write your code here

        File rootDir = new File("");
        String rootPath = rootDir.getAbsolutePath();
        System.out.println(rootPath);
    }
}
