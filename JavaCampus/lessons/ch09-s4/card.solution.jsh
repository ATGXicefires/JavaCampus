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

class MascotCheer implements Cheer {
    String mascot;
    String tip;

    MascotCheer(String mascot, String tip) {
        this.mascot = (mascot == null || mascot.trim().isEmpty()) ? "滷肉" : mascot.trim();
        this.tip = (tip == null || tip.trim().isEmpty()) ? "保持微笑" : tip.trim();
    }

    public String sender() {
        return mascot;
    }

    public String message(int round) {
        if (round <= 0) {
            return "準備上場！";
        }
        return "第" + round + "輪提醒：" + tip;
    }
}

class CheerBoard {
    Cheer[] cheers;

    CheerBoard(Cheer[] cheers) {
        this.cheers = cheers == null ? new Cheer[0] : cheers;
    }

    int count() {
        int c = 0;
        for (int i = 0; i < cheers.length; i++) {
            if (cheers[i] != null) {
                c++;
            }
        }
        return c;
    }

    String formatCard(int round) {
        int r = Math.max(1, round);
        String out = "=== 第 " + r + " 輪應援卡 ===";
        int valid = 0;
        for (int i = 0; i < cheers.length; i++) {
            Cheer ch = cheers[i];
            if (ch != null) {
                String rawSender = ch.sender();
                String safeSender = (rawSender == null || rawSender.trim().isEmpty()) ? "匿名" : rawSender.trim();
                out += "\n[" + safeSender + "] " + ch.message(r);
                valid++;
            }
        }
        if (valid == 0) {
            out += "\n(尚無應援訊息)";
        }
        return out;
    }
}
