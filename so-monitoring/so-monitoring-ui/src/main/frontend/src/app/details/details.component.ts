/**
============LICENSE_START=======================================================
 Copyright (C) 2018 Ericsson. All rights reserved.
================================================================================
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
 limitations under the License.

SPDX-License-Identifier: Apache-2.0
============LICENSE_END=========================================================

@authors: ronan.kenny@est.tech, waqas.ikram@est.tech
*/

import { Component, OnInit } from '@angular/core';
import { DataService } from '../data.service';
import { ActivatedRoute, Router } from "@angular/router";
import { BpmnInfraRequest } from '../model/bpmnInfraRequest.model';
import { ActivityInstance } from '../model/activityInstance.model';
import { ProcessInstanceDetail } from '../model/processInstance.model';
import { ProcessDefinitionDetail } from '../model/processDefinition.model';
import { CommonModule } from '@angular/common';
import Viewer from 'bpmn-js/lib/NavigatedViewer';
import { ViewEncapsulation } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { VariableInstance } from '../model/variableInstance.model';
import { ToastrNotificationService } from '../toastr-notification-service.service';
import { NgxSpinnerService } from 'ngx-spinner';
import { ElementRef, ViewChild } from '@angular/core';

@Component({
  selector: 'app-details',
  templateUrl: './details.component.html',
  styleUrls: ['./details.component.scss'],
  encapsulation: ViewEncapsulation.None
})

export class DetailsComponent implements OnInit {

  @ViewChild("canvas") elementReference: ElementRef;

  bpmnViewer: any;

  processInstanceID: string;

  processDefinitionID: string;

  processDefinitionName: string;

  activityInstance: ActivityInstance[];

  processInstance: ProcessInstanceDetail;

  processDefinition: ProcessDefinitionDetail;

  variableInstance: VariableInstance[];

  displayedColumns = ['activityId', 'activityName', 'activityType', 'startTime', 'endTime', 'durationInMillis'];

  displayedColumnsVariable = ['name', 'type', 'value'];

  constructor(private route: ActivatedRoute, private data: DataService, private popup: ToastrNotificationService,
    private router: Router, private spinner: NgxSpinnerService) { }

  async getActivityInstance(procInstId: string) {
    await this.data.getActivityInstance(procInstId).then(
      (data: ActivityInstance[]) => {
        this.activityInstance = data;
        console.log(data);
      }, error => {
        console.log(error);
        this.popup.error("Unable to get activity instance details for id: " + procInstId + " Error code:" + error.status);
      });
  }

  async getProcessDefinition(procDefId: string) {
    await this.data.getProcessDefinition(procDefId).subscribe(
      async (data: ProcessDefinitionDetail) => {
        this.processDefinition = data;
        console.log(data);
        await this.displayCamundaflow(this.processDefinition.processDefinitionXml, this.activityInstance,
          this.router, this.spinner, this.popup);
      }, error => {
        console.log(error);
        this.popup.error("Unable to get process definition for id: " + procDefId + " Error code:" + error.status);
      });
  }

  async getProcInstance(procInstId: string) {
    await this.data.getProcessInstance(procInstId).then(
      async (data: ProcessInstanceDetail) => {
        this.processInstance = data;
        this.processDefinitionID = this.processInstance.processDefinitionId;
        this.processDefinitionName = this.processInstance.processDefinitionName;
        console.log("Process definition id: " + this.processDefinitionID);
        await this.getActivityInstance(this.processInstanceID);
        await this.getProcessDefinition(this.processDefinitionID);
      }, error => {
        console.log(error);
        this.popup.error("Unable to get process instance for id: " + procInstId + " Error code:" + error.status);
      });
  }

  displayCamundaflow(bpmnXml, activities: ActivityInstance[], router: Router,
    spinner: NgxSpinnerService, popup: ToastrNotificationService) {
    spinner.show();

    this.bpmnViewer.importXML(bpmnXml, (error) => {
      if (error) {
        console.error('Unable to load BPMN flow ', error);
        popup.error('Unable to load BPMN flow ');
        spinner.hide();
      } else {
        spinner.hide();
        let canvas = this.bpmnViewer.get('canvas');
        var eventBus = this.bpmnViewer.get('eventBus');
        var elementRegistry = this.bpmnViewer.get('elementRegistry');
        var overlays = this.bpmnViewer.get('overlays');

        activities.forEach(a => {
          if (a.calledProcessInstanceId !== null) {
            var element = elementRegistry.get(a.activityId);
            let newNode = document.createElement('div');
            newNode.className = 'highlight-overlay';
            newNode.id = element.id;
            newNode.style.width = element.width + "px";
            newNode.style.height = element.height + "px";
            newNode.style.cursor = "pointer";

            overlays.add(a.activityId, {
              position: {
                top: -5,
                left: -5
              },
              html: newNode
            });

            newNode.addEventListener('click', function(e) {
              console.log("clicked on: " + e.srcElement.id)
              activities.forEach(a => {
                if (a.activityId == e.srcElement.id && a.calledProcessInstanceId !== null) {
                  console.log("will drill down to : " + a.calledProcessInstanceId);
                  router.navigate(['/details/' + a.calledProcessInstanceId]);
                }
              });
            });
          }
        });
        // zoom to fit full viewport
        canvas.zoom('fit-viewport', 'auto');
        activities.forEach(a => {
          canvas.addMarker(a.activityId, 'highlight');
        });
      }
    });
  }

  zoomIn() {
    this.bpmnViewer.get('zoomScroll').zoom(1, {
      x: this.elementReference.nativeElement.offsetWidth / 2,
      y: this.elementReference.nativeElement.offsetHeight / 2
    });
  }

  zoomOut() {
    this.bpmnViewer.get('zoomScroll').zoom(-1, {
      x: this.elementReference.nativeElement.offsetWidth / 2,
      y: this.elementReference.nativeElement.offsetHeight / 2
    });
  }
  resetZoom() {
    let canvas = this.bpmnViewer.get('canvas');
    canvas.resized();
    canvas.zoom('fit-viewport', 'auto');

  }

  getVarInst(procInstId: string) {
    this.data.getVariableInstance(procInstId).subscribe(
      (data: VariableInstance[]) => {
        this.variableInstance = [];
        for (let i = 0; i < data.length; i++) {
          var value = data[i]['value'];
          var type = data[i]['type'];
          if ((type == 'Object') && !(value == null)) {
            try {
              data[i]['value'] = JSON.stringify(value, null, 2);
            }
            catch (error) {
              console.log("Unable to \nError Code: " + error);
            }
          }
          this.variableInstance[i] = data[i];
        }
        console.log(data);
      }, error => {
        console.log(error);
        this.popup.error("Unable to get Variable instances for id: " + procInstId + " Error code:" + error.status);
      });
  }

  async ngOnInit() {
    this.bpmnViewer = new Viewer({
      container: '.canvas'
    });
    this.route.params.subscribe(
      async params => {
        this.processInstanceID = params.id as string;
        console.log("Will GET BpmnInfraRequest instance using id: " + this.processInstanceID);
        await this.getProcInstance(this.processInstanceID);

        this.getVarInst(this.processInstanceID);
      });
  }

}
