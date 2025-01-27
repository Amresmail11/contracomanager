export interface CreateRfiRequest {
  title: string;
  description: string;
  priority: string;
  projectCode: string;
  deadline?: string;
  assignedToUsername?: string;
  assignedGroupName?: string;
} 