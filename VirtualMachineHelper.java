package com.vmware.vim.samples;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;

//helper class for VirtualMachine vm related commands
public class VirtualMachineHelper {
    //handle different vm commands
    public static void handleVMCmd(String[] input, ManagedEntity[] managedEntities) {
        if (input.length == 1 && "vm".equals(input[0])) {
            enumerateVms(managedEntities);
        }
        else if (input.length == 3) {
            if("info".equals(input[2])) {
                showVmInfo(input[1], managedEntities);
            }
            else if("shutdown".equals(input[2])) {
                shutDown(input[1], managedEntities);
            }
            else if("on".equals(input[2])) {
                turnOn(input[1], managedEntities);

            }
            else if("off".equals(input[2])) {
                turnOff(input[1], managedEntities);
            }
        }
        else {
            System.out.println("Warning: Command not correct! Check command help.");
        }
    }
    // output of  vm command
    private static void enumerateVms(ManagedEntity[] managedEntities) {
        for (int i = 0; i<managedEntities.length;i++) {
            ManagedEntity managedEntity = managedEntities[i];
            VirtualMachine vmsystemobj = (VirtualMachine) managedEntity;
            System.out.println("vm[" + i + "]: Name = " + vmsystemobj.getName());
        }
    }
    // output of vm vname info command
    private static void showVmInfo(String vmName, ManagedEntity[] managedEntities) {
        //Get target virtual machine from input
        VirtualMachine vmInstance = getTargetVm(vmName, managedEntities);

        if(vmInstance == null) {
            System.out.println("Failed: Invalid virtual machine name = " + vmName);
            return;
        }

        System.out.println("Virtual Machine:");
        System.out.printf("%10s%s %s\n", "", "Name =", vmInstance.getName());

        VirtualMachineSummary vmSummary = vmInstance.getSummary();
        VirtualMachineConfigInfo vmConfig = vmInstance.getConfig();
        VirtualMachineGuestSummary vmGuestSummary = vmSummary.getGuest();
        String guestName = vmConfig.getGuestFullName();
        System.out.printf("%10s%s %s\n", "", "Guest full name =", guestName);

        String guestState = vmGuestSummary.getToolsStatus().toString();
        System.out.printf("%10s%s %s\n", "", "Guest state =", guestState);

        String ipAddress = vmGuestSummary.getIpAddress();
        System.out.printf("%10s%s %s\n", "", "Ip addr =", ipAddress);

        String toolRunningState = vmGuestSummary.getToolsRunningStatus();
        System.out.printf("%10s%s %s\n", "", "Tool running status =", toolRunningState);

        String powerState = vmInstance.getRuntime().getPowerState().toString();
        System.out.printf("%10s%s %s\n", "", "Power status =", powerState);

    }
    // output of vm vname shutdown command
    private static void shutDown(String vmName, ManagedEntity[] managedEntities) {
        //Get target virtual machine from input
        VirtualMachine vmInstance = getTargetVm(vmName, managedEntities);

        if(vmInstance == null) {
            System.out.println("Failed: Invalid virtual machine name = " + vmName);
            return;
        }

        System.out.println("Virtual Machine:");
        System.out.printf("%10s%s %s\n", "", "Name =", vmInstance.getName());
        try {
            VirtualMachineRuntimeInfo vmri = (VirtualMachineRuntimeInfo) vmInstance.getRuntime();
            if(vmri.getPowerState() == VirtualMachinePowerState.poweredOff)
            {
                System.out.println("Warning: Virtual Machine Guest"  + vmName + " is already stopped.");
            }
            else if (vmri.getPowerState() == VirtualMachinePowerState.poweredOn){
                vmInstance.shutdownGuest();
                SimpleDateFormat time_formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String current_time_str = time_formatter.format(System.currentTimeMillis());

                long start = System.currentTimeMillis();
                while(vmInstance.getRuntime().getPowerState() != VirtualMachinePowerState.poweredOff) {
                    Thread.sleep(2000);
                    if(System.currentTimeMillis() - start >= 3 * 60 * 1000) {
                        System.out.println("Graceful shutDown failed. Now try a hard power off.");
                        turnoffCore(vmName, vmInstance);
                        return;
                    }
                }
                System.out.println("Shutdown guest: completed, time = " + current_time_str);
            }
        } catch (Exception e) {
            System.out.println("Failed: Virtual Machine Guest"  + vmName + " shut down failed.");
        }
    }
    //output of vm vname poweron command
    private static void turnOn(String vmName, ManagedEntity[] managedEntities) {
        //Get target virtual machine from input
        VirtualMachine vmInstance = getTargetVm(vmName, managedEntities);

        if(vmInstance == null) {
            System.out.println("Failed: Invalid virtual machine name = " + vmName);
            return;
        }

        System.out.println("Virtual Machine:");
        System.out.printf("%10s%s %s\n", "", "Name =", vmInstance.getName());

        try {
            Task task = vmInstance.powerOnVM_Task(null);

            SimpleDateFormat time_formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            String result = task.waitForTask();
            TaskInfo info = task.getTaskInfo();
            String formatted = time_formatter.format(info.getCompleteTime().getTime());

            if (TaskInfoState.success.equals(info.getState())) { // success
                System.out.println("Power on VM: status = " + result + ", completion time = " + formatted);
            } else {
                System.out.println("Power on VM: status = "
                        + info.getError().getLocalizedMessage()
                        + ", completion time = " + info.getCompleteTime());
            }
        } catch (InterruptedException ex) {
            System.out.println("Failed: Virtual Machine Guest"  + vmName + " failed to start.");
        } catch (RemoteException ex) {
            System.out.println("Failed: Virtual Machine Guest"  + vmName + " connection failed.");
        }

    }
    //output of vm vname poweroff command
    private static void turnOff(String vmName, ManagedEntity[] managedEntities) {
        //Get target virtual machine from input
        VirtualMachine vmInstance = getTargetVm(vmName, managedEntities);

        if(vmInstance == null) {
            System.out.println("Failed: Invalid virtual machine name = " + vmName);
            return;
        }

        System.out.println("Virtual Machine:");
        System.out.printf("%10s%s %s\n", "", "Name =", vmInstance.getName());

        turnoffCore(vmName, vmInstance);
    }
    // split logic for reuse in shutdown
    private static void turnoffCore(String vmName, VirtualMachine vmInstance) {
        try {
            Task task = vmInstance.powerOffVM_Task();
            String result = task.waitForTask();
            TaskInfo info = task.getTaskInfo();

            SimpleDateFormat time_formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String formatted = time_formatter.format(info.getCompleteTime().getTime());

            if (TaskInfoState.success.equals(info.getState())) { // success
                System.out.println("Power off VM: status = " + result + ", completion time = " + formatted);
            } else {
                System.out.println("Power off VM: status = "
                        + info.getError().getLocalizedMessage()
                        + ", completion time = " + formatted);
            }

        } catch (InterruptedException ex) {
            System.out.println("Failed: Virtual Machine Guest"  + vmName + " failed to power off.");
        } catch (RemoteException ex) {
            System.out.println("Failed: Virtual Machine Guest"  + vmName + " connection failed.");
        }
    }
    // get my vm
    private static VirtualMachine getTargetVm(String vmName, ManagedEntity[] managedEntities) {
        VirtualMachine vm = null;
        if (vmName != null) {
            for (int i = 0; i<managedEntities.length;i++) {
                ManagedEntity managedEntity = managedEntities[i];
                VirtualMachine vmSystemObj = (VirtualMachine) managedEntity;
                if (vmName.equals(vmSystemObj.getName())) {
                    vm = vmSystemObj; break;
                }
            }
        }
        return vm;
    }
}
