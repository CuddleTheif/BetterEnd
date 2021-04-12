package ru.betterend.integration.rei;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;

import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.server.ContainerInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import ru.betterend.recipe.builders.AlloyingRecipe;
import ru.betterend.recipe.builders.InfusionRecipe;

public class REIInfusionDisplay implements TransferRecipeDisplay {
	
	private final InfusionRecipe recipe;
	private final List<List<EntryStack>> input;
	private final List<EntryStack> output;
	private final int time;
	
	public REIInfusionDisplay(InfusionRecipe recipe) {
		this.recipe = recipe;
		this.input = Lists.newArrayList();
		this.output = Collections.singletonList(EntryStack.create(recipe.getResultItem()));
		this.time = recipe.getInfusionTime();
		
		recipe.getIngredients().forEach(ingredient -> {
			input.add(EntryStack.ofIngredient(ingredient));
		});
	}
	
	public int getInfusionTime() {
		return this.time;
	}
	
	@Override
	public @NotNull Optional<ResourceLocation> getRecipeLocation() {
		return Optional.ofNullable(recipe).map(Recipe::getId);
	}

	@Override
	public @NotNull List<List<EntryStack>> getInputEntries() {
		return this.input;
	}
	
	@Override
	public @NotNull List<List<EntryStack>> getResultingEntries() {
		return Collections.singletonList(output);
	}

	@Override
	public @NotNull ResourceLocation getRecipeCategory() {
		return AlloyingRecipe.ID;
	}
	
	@Override
	public @NotNull List<List<EntryStack>> getRequiredEntries() {
		return this.input;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public List<List<EntryStack>> getOrganisedInputEntries(ContainerInfo<AbstractContainerMenu> containerInfo, AbstractContainerMenu container) {
		return this.input;
	}
}
