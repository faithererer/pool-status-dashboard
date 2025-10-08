package com.zjc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to handle Single Page Application (SPA) routing.
 * Forwards all non-API, non-static file requests to the root path,
 * allowing the client-side router (e.g., Vue Router) to handle them.
 */
@Controller
public class SpaController {

    /**
     * Forwards requests for top-level routes (e.g., /dashboard, /management).
     * The regex "[^\\.]*" matches any path that does not contain a dot,
     * thus avoiding interference with static assets like .js, .css, .ico files.
     *
     * @return A forward to the root path ("/").
     */
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String forward() {
        return "forward:/";
    }

    /**
     * Forwards requests for nested routes (e.g., /dashboard/details, /management/pools/1).
     * This ensures that refreshing the page on a nested client-side route
     * still correctly serves the SPA's entry point.
     *
     * @return A forward to the root path ("/").
     */
    @RequestMapping(value = "/**/{path:[^\\.]*}")
    public String forwardNested() {
        return "forward:/";
    }
}