package com.helospark.sparktemplatingplugin.handlers.templater;

import org.eclipse.core.commands.ExecutionEvent;

import bsh.EvalError;
import bsh.Interpreter;

public class ScriptInterpreter {
    private ScriptExposedObjectProvider scriptExposedObjectProvider;

    public ScriptInterpreter(ScriptExposedObjectProvider scriptExposedObjectProvider) {
        this.scriptExposedObjectProvider = scriptExposedObjectProvider;
    }

    public void interpret(ExecutionEvent event, String program) {
        try {
            Interpreter bsh = new Interpreter();
            scriptExposedObjectProvider.providerExposedObjects(event)
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        try {
                            bsh.set(entry.getKey(), entry.getValue());
                        } catch (EvalError e) {
                            throw new RuntimeException(e);
                        }
                    });

            bsh.eval(program);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
