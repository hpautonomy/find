package com.autonomy.abc.selenium.devconsole;

import com.autonomy.abc.selenium.application.Application;
import com.autonomy.abc.selenium.application.ApplicationType;
import com.autonomy.abc.selenium.control.Window;

public class DevConsole implements Application<DevConsoleElementFactory> {
    private DevConsoleElementFactory factory;

    public DevConsole(Window window) {
        inWindow(window);
    }

    @Override
    public DevConsoleElementFactory elementFactory() {
        return factory;
    }

    @Override
    public ApplicationType getType() {
        return ApplicationType.HOSTED;
    }

    @Override
    public DevConsole inWindow(Window window) {
        this.factory = new DevConsoleElementFactory(window.getSession().getDriver());
        return this;
    }
}
