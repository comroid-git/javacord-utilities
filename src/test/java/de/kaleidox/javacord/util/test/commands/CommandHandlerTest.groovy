package de.kaleidox.javacord.util.test.commands

import java.util.concurrent.CompletableFuture

import de.kaleidox.javacord.util.commands.Command
import de.kaleidox.javacord.util.commands.CommandGroup
import de.kaleidox.javacord.util.commands.CommandHandler
import de.kaleidox.javacord.util.test.dummy.DiscordApiDummy

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.ServerChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.Messageable
import org.javacord.api.entity.permission.PermissionType
import org.javacord.api.entity.user.User
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.core.entity.channel.ServerTextChannelImpl
import org.javacord.core.entity.message.MessageImpl
import org.javacord.core.entity.server.ServerImpl
import org.javacord.core.entity.user.UserImpl
import org.javacord.core.event.message.CertainMessageEventImpl
import org.javacord.core.event.message.MessageCreateEventImpl
import org.javacord.core.event.message.MessageEventImpl
import org.junit.Before
import org.junit.Test

import static org.easymock.EasyMock.createMockBuilder
import static org.easymock.EasyMock.expect
import static org.easymock.EasyMock.partialMockBuilder
import static org.easymock.EasyMock.replay
import static org.junit.Assert.assertEquals

class CommandHandlerTest {
    private DiscordApi api
    private CommandHandler handler

    @Before
    void setup() {
        api = new DiscordApiDummy()
        handler = new CommandHandler(api, false)
    }

    @Test
    void testCommandRegistration() {
        handler.registerCommands(new TestCommand())

        def commands = handler.getCommands()
        def any = commands.stream().findAny().orElseThrow(AssertionError::new)

        assertEquals 1, commands.size()
        assertEquals TestCommand.getMethod("cmd"), any.method
        assertEquals TestCommand, any.method.declaringClass
    }

    @Test
    void testCommandExecution() {
        CompletableFuture<Boolean> successFuture = new CompletableFuture<>()

        handler.registerCommands(new TestCommand())

        def mockUsr = partialMockBuilder(UserImpl)
                .niceMock()
        def mockMsg = partialMockBuilder(MessageImpl)
                .addMockedMethod(MessageImpl.getMethod("getContent"))
                .addMockedMethod(MessageImpl.getMethod("getReadableContent"))
                .addMockedMethod(MessageImpl.getMethod("getChannel"))
                .addMockedMethod(MessageImpl.getMethod("getUserAuthor"))
                .niceMock()
        def mockChl = createMockBuilder(ServerTextChannelImpl)
                .addMockedMethod(Messageable.getMethod("sendMessage", String))
                .addMockedMethod(ServerChannel.getMethod("hasAnyPermission", User, PermissionType[]))
                .niceMock()
        def mockSrv = partialMockBuilder(ServerImpl)
                .addMockedMethod(ServerImpl.getMethod("getId"))
                .niceMock()
        def mockEvent = partialMockBuilder(MessageCreateEventImpl)
                .addMockedMethod(CertainMessageEventImpl.getMethod("getMessage"))
                .addMockedMethod(MessageEventImpl.getMethod("getServer"))
                .niceMock()

        def returnCheck = new Messageable() {
            @Override
            CompletableFuture<Message> sendMessage(String content) {
                successFuture.complete(content == TestCommand.chk)

                return CompletableFuture.completedFuture(null)
            }
        }

        expect(mockMsg.getContent()).andReturn("!cmd").anyTimes()
        expect(mockMsg.getChannel()).andReturn(mockChl).anyTimes()
        expect(mockMsg.getUserAuthor()).andReturn(Optional.of(mockUsr)).anyTimes()

        expect(mockChl.sendMessage((String) null)).andDelegateTo(returnCheck)

        expect(mockSrv.getId()).andReturn(0).anyTimes()

        expect(mockEvent.getMessage()).andReturn(mockMsg).anyTimes()
        expect(mockEvent.getServer()).andReturn(Optional.of(mockSrv)).anyTimes()

        replay mockUsr
        replay mockMsg
        replay mockChl
        replay mockSrv
        replay mockEvent

        api.messageCreateListeners.forEach(listener -> {
            listener.onMessageCreate(mockEvent as MessageCreateEvent)
        })

        if (!successFuture.join())
            throw new RuntimeException("Test Failed! Wrong value was printed to channel")
    }

    @CommandGroup
    class TestCommand {
        private final static String chk = "mock successful"

        @Command
        String cmd() {
            return chk
        }
    }
}
