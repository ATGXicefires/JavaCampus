interface Cheer {
    String sender();
    String message(int round);
}

class Me implements Cheer {
    public String sender() {
        return "我";
    }

    public String message(int round) {
        if (round <= 1) {
            return "學長第一關加油！深呼吸別緊張！";
        }
        if (round == 2) {
            return "學長進入複試了！穩住節奏！";
        }
        return "學長最後一哩路！你一定可以！";
    }
}
