interface Cheer {
    void say();
}

class Me implements Cheer {
    public void say() {
        System.out.println("學長面試加油！你一定可以！");
    }
}

new Me().say();
