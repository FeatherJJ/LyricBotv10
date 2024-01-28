
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.MusicCommand;


public class SkipCmd extends MusicCommand 
{
    public SkipCmd(Bot bot)
    {
        super(bot);
        this.name = "skip";
        this.help = "votes to skip the current song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        RequestMetadata rm = handler.getRequestMetadata();
        if(event.getAuthor().getIdLong() == rm.getOwner())
        {
            event.reply(event.getClient().getSuccess()+" Skipped **"+handler.getPlayer().getPlayingTrack().getInfo().title+"**");
            handler.getPlayer().stopTrack();
        }
        else
        {
            int listeners = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
            String msg;
            if(handler.getVotes().contains(event.getAuthor().getId()))
                msg = event.getClient().getWarning()+" You already voted to skip this song `[";
            else
            {
                msg = event.getClient().getSuccess()+" You voted to skip the song `[";
                handler.getVotes().add(event.getAuthor().getId());
            }
            int skippers = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> handler.getVotes().contains(m.getUser().getId())).count();
            int required = (int)Math.ceil(listeners * bot.getSettingsManager().getSettings(event.getGuild()).getSkipRatio());
            msg += skippers + " votes, " + required + "/" + listeners + " needed]`";
            if(skippers>=required)
            {
                msg += "\n" + event.getClient().getSuccess() + " Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title
                    + "** " + (rm.getOwner() == 0L ? "(autoplay)" : "(requested by **" + rm.user.username + "**)");
                handler.getPlayer().stopTrack();
            }
            event.reply(msg);
        }
    }
    
}
