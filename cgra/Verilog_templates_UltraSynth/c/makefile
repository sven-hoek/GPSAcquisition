TARGET 		:= cgra_api_test

SRCDIR 		:= .
BUILDDIR 	:= obj
TARGETDIR 	:= bin
SRCEXT 		:= c
OBJEXT 		:= o

CC 			:= gcc
CFLAGS 		:= -Wall -g 

SRCS     	:= $(shell find $(SRCDIR) -type f -name '*.$(SRCEXT)')
OBJ     	:= $(patsubst $(SRCDIR)/%,$(BUILDDIR)/%,$(SRCS:.$(SRCEXT)=.$(OBJEXT)))

all: initdirs $(TARGET)
	@echo Finished Compilation

-include $(OBJ:%.o=%.d)
	
$(TARGET): $(OBJ)
	$(CC) $(CFLAGS) -o $(TARGETDIR)/$(TARGET) $^
	@echo Created $(TARGET)

$(BUILDDIR)/%.$(OBJEXT): $(SRCDIR)/%.$(SRCEXT)
	$(CC) $(CFLAGS) -MD -MP -c -o $@ $<

initdirs:
	@mkdir -p $(TARGETDIR)
	@mkdir -p $(BUILDDIR)

clean:
	rm -rf $(BUILDDIR) $(TARGETDIR)

.PHONY: clean initdirs