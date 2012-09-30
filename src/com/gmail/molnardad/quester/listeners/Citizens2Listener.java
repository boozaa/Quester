package com.gmail.molnardad.quester.listeners;

import java.util.List;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.gmail.molnardad.quester.QuestData;
import com.gmail.molnardad.quester.QuestHolder;
import com.gmail.molnardad.quester.QuestManager;
import com.gmail.molnardad.quester.Quester;
import com.gmail.molnardad.quester.QuesterTrait;
import com.gmail.molnardad.quester.exceptions.QuesterException;
import com.gmail.molnardad.quester.utils.Util;

public class Citizens2Listener implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLeftClick(NPCLeftClickEvent event) {
		if(event.getNPC().hasTrait(QuesterTrait.class)) {
			QuestManager qm = Quester.qMan;
			QuestHolder qh = qm.getHolder(event.getNPC().getTrait(QuesterTrait.class).getHolderID());
			Player player = event.getClicker();
			if(!Util.permCheck(player, QuestData.PERM_USE_NPC, true)) {
				return;
			}
			// If player has perms and holds blaze rod
			boolean isOp = Util.permCheck(player, QuestData.MODIFY_PERM, false);
			if(isOp) {
				if(player.getItemInHand().getTypeId() == 369) {
					event.getNPC().getTrait(QuesterTrait.class).setHolderID(-1);
					player.sendMessage(ChatColor.GREEN + "Holder unassigned.");
				    return;
				}
			}
			if(qh == null) {
				player.sendMessage(ChatColor.RED + "No quest holder assigned.");
				return;
			}
			try {
				qh.selectNext();
			} catch (QuesterException e) {
				player.sendMessage(e.message());
				if(!isOp) {
					return;
				}
				
			}
			
			player.sendMessage(Util.line(ChatColor.BLUE, event.getNPC().getName() + "'s quests", ChatColor.GOLD));
			if(isOp) {
				qh.showQuestsModify(player);
			} else {
				qh.showQuestsUse(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRightClick(NPCRightClickEvent event) {
		if(event.getNPC().hasTrait(QuesterTrait.class)) {
			QuestManager qm = Quester.qMan;
			QuestHolder qh = qm.getHolder(event.getNPC().getTrait(QuesterTrait.class).getHolderID());
			Player player = event.getClicker();
			if(!Util.permCheck(player, QuestData.PERM_USE_NPC, true)) {
				return;
			}
			boolean isOP = Util.permCheck(player, QuestData.MODIFY_PERM, false);
			// If player has perms and holds blaze rod
			if(isOP) {
				if(player.getItemInHand().getTypeId() == 369) {
					int sel = qm.getSelectedHolderID(player.getName());
					if(sel < 0){
						player.sendMessage(ChatColor.RED + "Holder not selected.");
					} else {
						event.getNPC().getTrait(QuesterTrait.class).setHolderID(sel);
						player.sendMessage(ChatColor.GREEN + "Holder assigned.");
					}
				    return;
				}
			}
			if(qh == null) {
				player.sendMessage(ChatColor.RED + "No quest holder assigned.");
				return;
			}
			int selected = qh.getSelected();
			List<Integer> qsts = qh.getQuests();
			int questID = qm.getPlayerQuest(player.getName()) == null ? -1 : qm.getPlayerQuest(player.getName()).getID();
			// player has quest and quest giver does not accept this quest
			if(questID >= 0 && !qsts.contains(questID)) {
				player.sendMessage(ChatColor.RED + "You can't complete your quest here.");
				return;
			}
			// player has quest and quest giver accepts this quest
			if(questID >= 0 && qsts.contains(questID)) {
				try {
					qm.complete(player, false);
				} catch (QuesterException e) {
					try {
						qm.showProgress(player);
					} catch (QuesterException f) {
						player.sendMessage(ChatColor.DARK_PURPLE + "Interesting error, you don't have and have quest at once !");
					}
				}
				return;
			}
			// player doesn't have quest
			if(qm.isQuestActive(selected)) {
				try {
					qm.startQuest(player, qm.getQuestNameByID(selected), false);
				} catch (QuesterException e) {
					player.sendMessage(e.message());
				}
			} else {
				player.sendMessage(ChatColor.RED + "No quest selected.");
			}
		}
	}
}
