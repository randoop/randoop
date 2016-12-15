package randoop.generation.types;

interface A1000 {}

interface A0100 {}

interface A0010 {}

interface A0001 {}

interface A1100 extends A1000, A0100 {}

interface A1010 extends A1000, A0010 {}

interface A1001 extends A1000, A0001 {}

interface A0110 extends A0100, A0010 {}

interface A0101 extends A0100, A0001 {}

interface A0011 extends A0010, A0001 {}

class A1110 implements A1100, A1010, A0110 {}

class A1101 implements A1100, A1001, A0101 {}

class A1011 implements A1010, A1001, A0011 {}

class A0111 implements A0110, A0101, A0011 {}
