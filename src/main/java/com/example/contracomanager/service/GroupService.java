package com.example.contracomanager.service;

import com.example.contracomanager.dto.group.CreateGroupRequest;
import com.example.contracomanager.dto.group.AddGroupMembersRequest;
import com.example.contracomanager.exception.ResourceNotFoundException;
import com.example.contracomanager.model.Group;
import com.example.contracomanager.model.GroupProjectUser;
import com.example.contracomanager.model.Project;
import com.example.contracomanager.model.User;
import com.example.contracomanager.repository.GroupProjectUserRepository;
import com.example.contracomanager.repository.GroupRepository;
import com.example.contracomanager.repository.ProjectRepository;
import com.example.contracomanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final GroupProjectUserRepository groupProjectUserRepository;
    private final ProjectService projectService;

    @Transactional
    public Map<String, String> createGroup(CreateGroupRequest request, User creator) {
        try {
            // Check if creator has access to the project
            if (!projectService.hasAccess(request.getProjectCode(), creator.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "You don't have access to project: " + request.getProjectCode());
            }

            // Find the project by code
            Project project = projectRepository.findByCode(request.getProjectCode())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with code: " + request.getProjectCode()));

            // Find all users by usernames or emails
            List<User> members = new ArrayList<>();
            for (String identifier : request.getMemberUsernames()) {
                User user;
                if (identifier.contains("@")) {
                    user = userRepository.findByEmail(identifier)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + identifier));
                } else {
                    user = userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + identifier));
                }
                members.add(user);
            }

            // Get all project members
            List<User> projectUsers = userRepository.findAllByProjectId(project.getId());
            
            // Check each member to see if they're in the project
            List<String> nonMemberUsernames = new ArrayList<>();
            for (User member : members) {
                boolean isMember = projectUsers.stream()
                    .anyMatch(projectUser -> projectUser.getId().equals(member.getId()));
                if (!isMember) {
                    nonMemberUsernames.add(member.getUsername());
                }
            }
            
            if (!nonMemberUsernames.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "The following users are not members of the project: " + String.join(", ", nonMemberUsernames));
            }

            // Create and save the group
            final Group group = Group.builder()
                .name(request.getName())
                .project(project)
                .createdBy(creator)
                .build();
            final Group savedGroup = groupRepository.save(group);

            // Create GroupProjectUser entries for all members
            List<GroupProjectUser> groupProjectUsers = members.stream()
                .map(member -> GroupProjectUser.builder()
                    .id(UUID.randomUUID())
                    .group(savedGroup)
                    .project(project)
                    .user(member)
                    .createdAt(LocalDateTime.now())
                    .build())
                .collect(Collectors.toList());
            groupProjectUserRepository.saveAll(groupProjectUsers);

            // Add creator as a member if not already included
            if (!members.contains(creator)) {
                GroupProjectUser creatorMembership = GroupProjectUser.builder()
                    .id(UUID.randomUUID())
                    .group(savedGroup)
                    .project(project)
                    .user(creator)
                    .createdAt(LocalDateTime.now())
                    .build();
                groupProjectUserRepository.save(creatorMembership);
            }

            // Return simple success message using HashMap instead of Map.of()
            Map<String, String> response = new HashMap<>();
            response.put("message", "Group created successfully");
            return response;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create group: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserGroups(User user) {
        List<GroupProjectUser> userGroupProjects = groupProjectUserRepository.findAllByUserId(user.getId());
        
        List<Map<String, Object>> groups = userGroupProjects.stream()
            .map(gpu -> {
                Group group = gpu.getGroup();
                Project project = gpu.getProject();
                User creator = group.getCreatedBy();
                
                List<GroupProjectUser> groupMembers = groupProjectUserRepository.findAllByGroupId(group.getId());
                List<Map<String, Object>> memberDetails = groupMembers.stream()
                    .map(member -> {
                        Map<String, Object> memberMap = new HashMap<>();
                        memberMap.put("id", member.getUser().getId().toString());
                        memberMap.put("username", member.getUser().getUsername());
                        memberMap.put("email", member.getUser().getEmail());
                        memberMap.put("job", member.getUser().getJob());
                        return memberMap;
                    })
                    .collect(Collectors.toList());

                Map<String, Object> groupMap = new HashMap<>();
                groupMap.put("id", group.getId().toString());
                groupMap.put("name", group.getName());
                groupMap.put("projectCode", project.getCode());
                groupMap.put("projectName", project.getName());
                groupMap.put("createdById", creator.getId().toString());
                groupMap.put("createdByUsername", creator.getUsername());
                groupMap.put("createdAt", group.getCreatedAt().toString());
                groupMap.put("totalMembers", groupMembers.size());
                groupMap.put("members", memberDetails);
                return groupMap;
            })
            .collect(Collectors.toList());

        // Calculate total members across all groups
        int totalMembers = groups.stream()
            .mapToInt(group -> (Integer) group.get("totalMembers"))
            .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("totalGroups", groups.size());
        response.put("totalMembers", totalMembers);
        response.put("groups", groups);
        
        return response;
    }

    @Transactional
    public Map<String, String> deleteGroup(UUID groupId, User user) {
        // Find the group
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        
        // Check if user is the creator
        if (!group.getCreatedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Only the group creator can delete the group");
        }

        // Delete all group-project-user associations first
        groupProjectUserRepository.deleteAllByGroupId(groupId);
        
        // Delete the group
        groupRepository.delete(group);
        
        // Return success message
        Map<String, String> response = new HashMap<>();
        response.put("message", "Group deleted successfully");
        return response;
    }

    @Transactional
    public Map<String, String> addGroupMembers(UUID groupId, AddGroupMembersRequest request, User user) {
        // Find the group
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        
        // Check if user is the creator
        if (!group.getCreatedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Only the group creator can add members");
        }

        // Find all users by usernames
        List<User> newMembers = userRepository.findAllByUsernameIn(request.getMemberUsernames());
        
        // Verify all usernames were found
        if (newMembers.size() != request.getMemberUsernames().size()) {
            List<String> foundUsernames = newMembers.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
            List<String> notFound = new ArrayList<>(request.getMemberUsernames());
            notFound.removeAll(foundUsernames);
            throw new ResourceNotFoundException("Users not found: " + String.join(", ", notFound));
        }

        // Get all project members
        List<User> projectUsers = userRepository.findAllByProjectId(group.getProject().getId());
        
        // Check each member to see if they're in the project
        List<String> nonMemberUsernames = new ArrayList<>();
        for (User member : newMembers) {
            boolean isMember = projectUsers.stream()
                .anyMatch(projectUser -> projectUser.getId().equals(member.getId()));
            if (!isMember) {
                nonMemberUsernames.add(member.getUsername());
            }
        }
        
        if (!nonMemberUsernames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "The following users are not members of the project: " + String.join(", ", nonMemberUsernames));
        }

        // Get existing group members to avoid duplicates
        List<User> existingMembers = groupProjectUserRepository.findAllByGroupId(groupId)
            .stream()
            .map(GroupProjectUser::getUser)
            .collect(Collectors.toList());

        // Filter out users that are already members
        List<User> membersToAdd = newMembers.stream()
            .filter(member -> existingMembers.stream()
                .noneMatch(existing -> existing.getId().equals(member.getId())))
            .collect(Collectors.toList());

        if (membersToAdd.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "All specified users are already members of the group");
        }

        // Create GroupProjectUser entries for new members
        List<GroupProjectUser> groupProjectUsers = membersToAdd.stream()
            .map(member -> GroupProjectUser.builder()
                .group(group)
                .project(group.getProject())
                .user(member)
                .build())
            .collect(Collectors.toList());
        groupProjectUserRepository.saveAll(groupProjectUsers);

        // Return success message
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Successfully added %d new members to the group", membersToAdd.size()));
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProjectGroups(String projectCode, User user) {
        // Check if user has access to the project
        if (!projectService.hasAccess(projectCode, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "You don't have access to project: " + projectCode);
        }

        // Find project
        Project project = projectRepository.findByCode(projectCode)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with code: " + projectCode));

        // Get all groups in the project
        List<Group> projectGroups = groupRepository.findByProjectId(project.getId());
        
        List<Map<String, Object>> groups = projectGroups.stream()
            .map(group -> {
                List<GroupProjectUser> groupMembers = groupProjectUserRepository.findAllByGroupId(group.getId());
                List<Map<String, Object>> memberDetails = groupMembers.stream()
                    .map(gpu -> {
                        User member = gpu.getUser();
                        Map<String, Object> memberMap = new HashMap<>();
                        memberMap.put("id", member.getId().toString());
                        memberMap.put("username", member.getUsername());
                        memberMap.put("email", member.getEmail());
                        memberMap.put("job", member.getJob());
                        return memberMap;
                    })
                    .collect(Collectors.toList());

                Map<String, Object> groupMap = new HashMap<>();
                groupMap.put("id", group.getId().toString());
                groupMap.put("name", group.getName());
                groupMap.put("projectCode", project.getCode());
                groupMap.put("createdBy", Map.of(
                    "id", group.getCreatedBy().getId().toString(),
                    "username", group.getCreatedBy().getUsername(),
                    "email", group.getCreatedBy().getEmail()
                ));
                groupMap.put("totalMembers", groupMembers.size());
                groupMap.put("members", memberDetails);
                return groupMap;
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("totalGroups", groups.size());
        response.put("groups", groups);
        
        return response;
    }
} 