package com.kerem.phinance.service;

import com.kerem.phinance.dto.GoalContributionDto;
import com.kerem.phinance.dto.GoalDto;
import com.kerem.phinance.exception.BadRequestException;
import com.kerem.phinance.exception.ResourceNotFoundException;
import com.kerem.phinance.model.Goal;
import com.kerem.phinance.model.GoalContribution;
import com.kerem.phinance.repository.GoalContributionRepository;
import com.kerem.phinance.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository contributionRepository;

    public List<GoalDto> getAllGoals(String userId) {
        return goalRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<GoalDto> getActiveGoals(String userId) {
        return goalRepository.findByUserIdAndCompletedFalse(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public GoalDto getGoalById(String userId, String goalId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));
        return mapToDto(goal);
    }

    public GoalDto createGoal(String userId, GoalDto dto) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setName(dto.getName());
        goal.setDescription(dto.getDescription());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setDeadline(dto.getDeadline());
        goal.setPriority(dto.getPriority());
        goal.setAccountId(dto.getAccountId());
        goal.setDependencyGoalIds(dto.getDependencyGoalIds());
        goal.setColor(dto.getColor());
        goal.setIcon(dto.getIcon());

        Goal saved = goalRepository.save(goal);
        return mapToDto(saved);
    }

    public GoalDto updateGoal(String userId, String goalId, GoalDto dto) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        goal.setName(dto.getName());
        goal.setDescription(dto.getDescription());
        goal.setTargetAmount(dto.getTargetAmount());
        goal.setDeadline(dto.getDeadline());
        goal.setPriority(dto.getPriority());
        goal.setAccountId(dto.getAccountId());
        goal.setDependencyGoalIds(dto.getDependencyGoalIds());
        goal.setColor(dto.getColor());
        goal.setIcon(dto.getIcon());

        Goal saved = goalRepository.save(goal);
        return mapToDto(saved);
    }

    public void deleteGoal(String userId, String goalId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        // Check if any goals depend on this one
        List<Goal> dependentGoals = goalRepository.findByDependencyGoalIdsContaining(goalId);
        if (!dependentGoals.isEmpty()) {
            throw new BadRequestException("Cannot delete goal with dependent goals");
        }

        goalRepository.delete(goal);
    }

    public GoalDto addContribution(String userId, GoalContributionDto dto) {
        Goal goal = goalRepository.findByIdAndUserId(dto.getGoalId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", dto.getGoalId()));

        // Create contribution record
        GoalContribution contribution = new GoalContribution();
        contribution.setGoalId(dto.getGoalId());
        contribution.setUserId(userId);
        contribution.setAmount(dto.getAmount());
        contribution.setNote(dto.getNote());
        contributionRepository.save(contribution);

        // Update goal current amount
        goal.setCurrentAmount(goal.getCurrentAmount().add(dto.getAmount()));

        // Check if goal is completed
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCompleted(true);
        }

        Goal saved = goalRepository.save(goal);
        return mapToDto(saved);
    }

    public GoalDto markAsCompleted(String userId, String goalId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        // Check if dependencies are completed
        if (goal.getDependencyGoalIds() != null && !goal.getDependencyGoalIds().isEmpty()) {
            for (String dependencyId : goal.getDependencyGoalIds()) {
                Goal dependency = goalRepository.findById(dependencyId)
                        .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", dependencyId));
                if (!dependency.isCompleted()) {
                    throw new BadRequestException("Cannot complete goal: dependency '" + dependency.getName() + "' is not completed");
                }
            }
        }

        goal.setCompleted(true);
        Goal saved = goalRepository.save(goal);
        return mapToDto(saved);
    }

    public boolean validateGoalDependencies(String userId, String goalId) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));

        if (goal.getDependencyGoalIds() == null || goal.getDependencyGoalIds().isEmpty()) {
            return true;
        }

        for (String dependencyId : goal.getDependencyGoalIds()) {
            Goal dependency = goalRepository.findById(dependencyId).orElse(null);
            if (dependency == null || !dependency.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    private GoalDto mapToDto(Goal goal) {
        GoalDto dto = new GoalDto();
        dto.setId(goal.getId());
        dto.setName(goal.getName());
        dto.setDescription(goal.getDescription());
        dto.setTargetAmount(goal.getTargetAmount());
        dto.setCurrentAmount(goal.getCurrentAmount());
        dto.setDeadline(goal.getDeadline());
        dto.setPriority(goal.getPriority());
        dto.setAccountId(goal.getAccountId());
        dto.setDependencyGoalIds(goal.getDependencyGoalIds());
        dto.setCompleted(goal.isCompleted());
        dto.setColor(goal.getColor());
        dto.setIcon(goal.getIcon());
        dto.setProgressPercentage(goal.getProgressPercentage());
        return dto;
    }
}
