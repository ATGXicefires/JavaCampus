int perWeek = perDay * days;
boolean tooMany = perWeek >= 10;
if (tooMany) {
    System.out.println("媽，明天便當多做一份。");
} else {
    System.out.println("再觀察一週。");
}
