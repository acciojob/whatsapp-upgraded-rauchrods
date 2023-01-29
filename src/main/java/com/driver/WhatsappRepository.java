package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository {

    private int currentGroup;
    private int currentMessage;
    private HashMap<String,User> userHashMap ;
    private HashMap<Group,List<User>> groupHashMap;

    private HashMap<Group,List<Message>> groupMessageHashMap;

    private HashMap<User,List<Message>> userMessageHashMap;

    private List<Message> messageList;

    public WhatsappRepository() {
        this.userHashMap = new HashMap<>();
        this.groupHashMap = new HashMap<>();
        this.messageList = new ArrayList<>();
        this.groupMessageHashMap = new HashMap<>();
        this.userMessageHashMap = new HashMap<>();
        this.currentGroup=0;
        this.currentMessage=0;
    }

    public String createUser(String name, String mobile) throws Exception {
          if(userHashMap.containsKey(mobile)){
              throw new Exception("User already exists");
          }

          userHashMap.put(mobile,new User(name,mobile));
          return "SUCCESS";
    }

    public Group createGroup(List<User> users){
        int noOfUsers = users.size();
        for(User user:users){
            if(!userHashMap.containsKey(user.getMobile())){
                userHashMap.put(user.getMobile(),user);
            }
        }
         Group group = null;
        if(noOfUsers==2){
             group = new Group(users.get(1).getName(),noOfUsers);
        }
        else {
            currentGroup++;
            String name  = "Group " + Integer.toString(currentGroup);
             group = new Group(name,noOfUsers);
        }

        groupHashMap.put(group,users);
        return group;
    }


    public int createMessage(String content){
        currentMessage++;
        Message message = new Message(currentMessage,content);
        messageList.add(message);
        return currentMessage;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{

         Group existingGrp = null;

         for(Group group1: groupHashMap.keySet()){
             if(group1.getName().equals(group.getName())){
                 existingGrp=group;
                 break;
             }
         }
         if(existingGrp==null){
             throw new Exception("Group does not exist");
         }

         List<User> userList = groupHashMap.get(existingGrp);
         User exiSender=null;
         for(User user: userList){
             if(user.getMobile().equals(sender.getMobile())){
                 exiSender=sender;
                 break;
             }
         }

         if(exiSender==null){
             throw new Exception("You are not allowed to send message");
         }

         messageList.add(message);
         if(!userMessageHashMap.containsKey(exiSender)){
             userMessageHashMap.put(exiSender,new ArrayList<>());
         }
         userMessageHashMap.get(exiSender).add(message);


         if(!groupMessageHashMap.containsKey(existingGrp)){
             groupMessageHashMap.put(existingGrp,new ArrayList<>());
         }
         groupMessageHashMap.get(existingGrp).add(message);

         return  groupMessageHashMap.get(existingGrp).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{

        Group exisGrp = null ;
        for(Group group1:groupHashMap.keySet()){
            if (group1.getName().equals(group.getName())){
                exisGrp=group;
                break;
            }
        }
        if(exisGrp==null){
            throw new Exception("Group does not exist");
        }

        List<User> userList = groupHashMap.get(exisGrp);
        if(!userList.get(0).getMobile().equals(approver.getMobile())){
            throw new Exception("Approver does not have rights");
        }

        User exiUser=null;
        int exisUserindex=-1;
        for(User user1: userList){
            if(user1.getMobile().equals(user.getMobile())){
                exiUser=user;
                break;
            }
        }
        if(exiUser==null){
            throw new Exception("User is not a participant");
        }
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).getMobile().equals(exiUser.getMobile())){
                exisUserindex=i;
                break;
            }
        }
        Collections.swap(userList,0,exisUserindex);

        return "SUCCESS";
    }


    public int removeUser(User user) throws Exception{
        Group exisGrp=null;
        for(Group group: groupHashMap.keySet()){
            List<User> userList = groupHashMap.get(group);

            for(User user1: userList){
                if(user1.getMobile().equals(user.getMobile())){
                    exisGrp=group;
                    break;
                }
            }
            if(exisGrp!=null){
                break;
            }
        }
        if(exisGrp==null){
            throw  new Exception("User not found");
        }

        List<User> userList = groupHashMap.get(exisGrp);

        if(userList.get(0).getMobile().equals(user.getMobile())){
            throw  new Exception("Cannot remove admin");
        }

        userList.remove(user);
        groupHashMap.put(exisGrp,userList);

        List<Message> grpmessageList = groupMessageHashMap.get(exisGrp);
        List<Message> usermessageList = userMessageHashMap.get(user);
        for(Message message: grpmessageList){
            if(usermessageList.contains(message)){
                grpmessageList.remove(message);
            }
        }
        groupMessageHashMap.put(exisGrp,grpmessageList);

        userMessageHashMap.remove(user);
        userHashMap.remove(user.getMobile());

        for(Message message: messageList){
            if(usermessageList.contains(message)){
                messageList.remove(message);
            }
        }

        int usersInGroup=groupHashMap.get(exisGrp).size();
        int msgsInGroup=groupMessageHashMap.get(exisGrp).size();
        int overallmsgs=messageList.size();

        return usersInGroup + msgsInGroup + overallmsgs;
    }

    public String findMessage(Date start, Date end, int K) throws Exception{
      List<Message> elligiblemessagelist = new ArrayList<>();
      for(Message message: messageList){
          if(message.getTimestamp().compareTo(start)>0 && message.getTimestamp().compareTo(end)<0){
              elligiblemessagelist.add(message);
          }
      }
      if(elligiblemessagelist.size()<K){
          throw new Exception("K is greater than the number of messages");
      }

      Collections.sort(elligiblemessagelist, new Comparator<Message>() {
          @Override
          public int compare(Message o1, Message o2) {
              return o1.getTimestamp().compareTo(o2.getTimestamp());
          }
      });
      return elligiblemessagelist.get(K-1).getContent();
    }
}
