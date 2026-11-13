interface Cheer {
    String sender();
    String message(int round);
}

// 下面這段實作違反了 Cheer 介面的契約，請修好它！
class Me implements Cheer {
    void massage(int round) {
        System.out.println("學長面試加油！你一定可以！");
    }
}
