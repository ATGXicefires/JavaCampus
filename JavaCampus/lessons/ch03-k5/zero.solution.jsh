try {
    System.out.println("平均：" + total / count);
} catch (ArithmeticException e) {
    System.out.println("還沒有成績");
}
