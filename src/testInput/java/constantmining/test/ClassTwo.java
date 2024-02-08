package constantmining.test;

import randoop.util.ClassFileConstants;

public class ClassTwo {
//    public int aInteger = 0;
//    public String bString = "b";
//    public short cShort = 1;
//    public long dLong = 2;
//    public float eFloat = 3;
//    public double fDouble = 4;
//    public char gChar = 5;
//    public byte hByte = 6;
//    public boolean iBoolean = false;
    public Integer jInteger = 7;
//    public String kString = "k";
//    public Short lShort = 8;
//    public Long mLong = 9L;
//    public Float nFloat = 10f;
//    public Double oDouble = 11d;
//    public Character pCharacter = 12;
//    public Byte qByte = 13;
//    public Boolean rBoolean = true;
//    public String sString = null;
    public Class<ClassFileConstants> cClass = ClassFileConstants.class;
    enum Level {
        LOW,
        MEDIUM,
        HIGH
    }

    public void doSomething() {
//        aInteger = 100;
//        bString = "c";
//        cShort = 101;
//        dLong = 102;
//        eFloat = 103;
//        fDouble = 104;
//        gChar = 105;
//        hByte = 106;
//        iBoolean = true;
//        jInteger = 107;
//        kString = "l";
//        lShort = 108;
//        mLong = 109L;
//        nFloat = 110f;
//        oDouble = 111d;
//        pCharacter = 112;
//        qByte = 113;
//        rBoolean = false;
//        sString = "t";
        cClass = ClassFileConstants.class;
//        System.out.println(Level.HIGH);
//        System.out.println("doSomething1");
        jInteger = jInteger + 100000;
    }

    // A dummy method that take the Enum as input
    public void doSomething2(Level level) {
        if (level == Level.LOW)
            System.out.println(level);
        else if (level == Level.MEDIUM)
            System.out.println(level);
        else if (level == Level.HIGH)
            System.out.println(level);
    }
}
