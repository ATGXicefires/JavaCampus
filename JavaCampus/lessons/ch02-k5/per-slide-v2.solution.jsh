int seconds = totalMinutes * 60;
int perSlide = seconds / slides;
int extraSec = seconds % slides;
int ceilSec = (seconds + slides - 1) / slides;
