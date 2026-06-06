package edu.bookingtour.util;

import edu.bookingtour.entity.DiemDen;
import org.springframework.stereotype.Component;

@Component("nav")
public class NavViewHelper {

    public String destinationThumb(DiemDen d) {
        return NavDestinationUtil.destinationThumb(d);
    }
}
