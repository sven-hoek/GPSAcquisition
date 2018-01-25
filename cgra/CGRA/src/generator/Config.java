package generator;

public abstract class Config<ConfOpt extends Enum> {

    private ConfOpt invalid;

    Config(ConfOpt invalid) {
        this.invalid = invalid;
    }

    protected abstract boolean parseOption(ConfOpt option, String arg);

    public abstract Class<ConfOpt> getEnumClass();

    public ConfOpt getInvalid() {
        return invalid;
    }
}
