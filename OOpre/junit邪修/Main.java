//重新理解一下String和String[]以及二维数组和一维数组
//String是单词，String[]是字符串数组,二维数组相应的就是n*n,String[]是一维数组

import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        AdventureContainer roster = new AdventureContainer();
        Scanner scanner = new Scanner(System.in);
        ArrayList<ArrayList<String>> inputInfo = new ArrayList<>();
        int n = Integer.parseInt(scanner.nextLine().trim());  //trim舍弃多余的空格
        for (int i = 0; i < n; i++) {
            String thisLine = scanner.nextLine();
            String[] strings = thisLine.trim().split(" +");
            inputInfo.add(new ArrayList<>(Arrays.asList(strings)));
        }
        //类似define简化计算步骤
        Input.exe(inputInfo, n, roster);
    }
}
